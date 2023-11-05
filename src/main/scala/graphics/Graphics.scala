// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Graphics extends Module {
  def incrDiv(da: Int, b: Int, quo: SInt, rem: SInt) = {
    val dquo = da / b
    var drem = da % b
    val rquo = WireDefault(quo + dquo.S)
    val rrem = WireDefault(rem + drem.S)
    if (drem > 0) {
      when (rem >= (b - drem).S) {
        rquo := quo + dquo.S + 1.S
        rrem := rem - b.S + drem.S
      }
    } else {
      when (rem <= -(b + drem).S) {
        rquo := quo + dquo.S - 1.S
        rrem := rem + b.S + drem.S
      }
    }
    (rquo, rrem)
  }

  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = new WrAxi(Vram.addrWidth, Vram.dataWidth)
    val done = Output(Bool())
  })

  val x0 = 512
  val y0 = 192

  val x1 = 290
  val y1 = 576

  val x2 = 734
  val y2 = 576

  val dx0 = x1 - x0
  val dx1 = x2 - x1
  val dx2 = x0 - x2

  val dy0 = y0 - y1
  val dy1 = y1 - y2
  val dy2 = y2 - y0

  val re0 = dx0 * y0 + dy0 * x0
  val re1 = dx1 * y1 + dy1 * x1
  val re2 = dx2 * y2 + dy2 * x2

  val a = dy0 * dx2 - dx0 * dy2

  val rr = re1 * 255 / a
  val rg = re2 * 255 / a
  val rb = re0 * 255 / a

  val rgbWidth = signedBitLength(2 * (VgaTiming.width - 1) * (VgaTiming.height - 1) * 255 / a)
  val r = RegInit(rr.S(rgbWidth.W))
  val g = RegInit(rg.S(rgbWidth.W))
  val b = RegInit(rb.S(rgbWidth.W))

  val rer = re1 * 255 % a
  val reg = re2 * 255 % a
  val reb = re0 * 255 % a

  val er = RegInit(rer.S(signedBitLength(a - 1).W))
  val eg = RegInit(reg.S(signedBitLength(a - 1).W))
  val eb = RegInit(reb.S(signedBitLength(a - 1).W))

  val eWidth = signedBitLength(2 * (VgaTiming.width - 1) * (VgaTiming.height - 1))
  val e0 = RegInit(re0.S(eWidth.W))
  val e1 = RegInit(re1.S(eWidth.W))
  val e2 = RegInit(re2.S(eWidth.W))

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    e0  := re0.S
    e1  := re1.S
    e2  := re2.S
    r   := rr.S
    g   := rg.S
    b   := rb.S
    er  := rer.S
    eg  := reg.S
    eb  := reb.S
    col := 0.U
    row := 0.U
  }

  val tileBuffer = Module(new TileBuffer)
  val valid = row < Tile.nrRows.U
  tileBuffer.io.inReq.valid := valid
  when (valid && tileBuffer.io.inReq.ready) {
    e0  := e0 - (dy0 * Tile.size).S
    e1  := e1 - (dy1 * Tile.size).S
    e2  := e2 - (dy2 * Tile.size).S
    val (rquo, rrem) = incrDiv(-dy1 * Tile.size * 255, a, r, er)
    r  := rquo
    er := rrem
    val (gquo, grem) = incrDiv(-dy2 * Tile.size * 255, a, g, eg)
    g  := gquo
    eg := grem
    val (bquo, brem) = incrDiv(-dy0 * Tile.size * 255, a, b, eb)
    b  := bquo
    eb := brem
    col := col + 1.U
    when (col === (Tile.nrCols - 1).U) {
      e0  := e0 + ((dy0 * (Tile.nrCols - 1) - dx0) * Tile.size).S
      e1  := e1 + ((dy1 * (Tile.nrCols - 1) - dx1) * Tile.size).S
      e2  := e2 + ((dy2 * (Tile.nrCols - 1) - dx2) * Tile.size).S
      val (rquo, rrem) = incrDiv((dy1 * (Tile.nrCols - 1) - dx1) * Tile.size * 255, a, r, er)
      r  := rquo
      er := rrem
      val (gquo, grem) = incrDiv((dy2 * (Tile.nrCols - 1) - dx2) * Tile.size * 255, a, g, eg)
      g  := gquo
      eg := grem
      val (bquo, brem) = incrDiv((dy0 * (Tile.nrCols - 1) - dx0) * Tile.size * 255, a, b, eb)
      b  := bquo
      eb := brem
      col := 0.U
      row := row + 1.U
    }
  }


  for (i <- 0 until Tile.size) {
    for (j <- 0 until Tile.size) {
      val pe0 = e0 - (dx0 * i + dy0 * j).S
      val pe1 = e1 - (dx1 * i + dy1 * j).S
      val pe2 = e2 - (dx2 * i + dy2 * j).S
      val visible = pe0 >= 0.S && pe1 >= 0.S && pe2 >= 0.S
      val (rquo, _) = incrDiv(-(dx1 * i + dy1 * j) * 255, a, r, er)
      val (gquo, _) = incrDiv(-(dx2 * i + dy2 * j) * 255, a, g, eg)
      val (bquo, _) = incrDiv(-(dx0 * i + dy0 * j) * 255, a, b, eb)
      tileBuffer.io.inReq.bits(i)(j).r := Mux(visible, rquo.asUInt, 0.U)
      tileBuffer.io.inReq.bits(i)(j).g := Mux(visible, gquo.asUInt, 0.U)
      tileBuffer.io.inReq.bits(i)(j).b := Mux(visible, bquo.asUInt, 0.U)
    }
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req <> tileBuffer.io.outReq
  io.done := !valid && fbWriter.io.done
}
