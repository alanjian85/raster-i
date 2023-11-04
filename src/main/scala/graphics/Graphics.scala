// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Graphics extends Module {
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
    col := col + 1.U
    when (col === (Tile.nrCols - 1).U) {
      e0  := e0 + ((dy0 * (Tile.nrCols - 1) - dx0) * Tile.size).S
      e1  := e1 + ((dy1 * (Tile.nrCols - 1) - dx1) * Tile.size).S
      e2  := e2 + ((dy2 * (Tile.nrCols - 1) - dx2) * Tile.size).S
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
      tileBuffer.io.inReq.bits(i)(j).r := Mux(visible, 255.U, 0.U)
      tileBuffer.io.inReq.bits(i)(j).g := Mux(visible, 255.U, 0.U)
      tileBuffer.io.inReq.bits(i)(j).b := Mux(visible, 255.U, 0.U)
    }
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req <> tileBuffer.io.outReq
  io.done := !valid && fbWriter.io.done
}
