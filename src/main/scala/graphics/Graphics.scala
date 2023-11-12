// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Graphics extends Module {
  def incrDiv(dquo: SInt, drem: SInt, divisor: SInt, quo: SInt, rem: SInt) = {
    val rquo = WireDefault(quo + dquo)
    val rrem = WireDefault(rem + drem)
    when (drem > 0.S && rem >= divisor - drem) {
      rquo := quo + dquo + 1.S
      rrem := rem - divisor + drem
    }
    when (drem < 0.S && rem <= -divisor - drem) {
      rquo := quo + dquo - 1.S
      rrem := rem + divisor + drem
    }
    (rquo, rrem)
  }

  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = new WrAxi(Vram.addrWidth, Vram.dataWidth)
    val done = Output(Bool())
  })

  val diffInfos = Wire(Vec(360, new DiffInfo))
  for (i <- 0 until 360) {
    val angle = math.toRadians(i)

    val x0 = 512
    val y0 = 192

    val z1 = 1 / (2 - math.sin(angle))
    val x1 = 512 - (222 * z1 * math.cos(angle)).toInt
    val y1 = 384 + (192 * z1).toInt

    val z2 = 1 / (2 + math.sin(angle))
    val x2 = 512 + (222 * z2 * math.cos(angle)).toInt
    val y2 = 384 + (192 * z2).toInt

    diffInfos(i) := DiffInfo.build((x0, y0), (x1, y1), (x2, y2))
  }

  val cntReg = RegInit(0.U(unsignedBitLength(1388888).W))
  val angle = RegInit(0.U(log2Up(360).W))
  cntReg := cntReg + 1.U
  when (cntReg === 1388888.U) {
    cntReg := 0.U
    angle := angle + 1.U
    when (angle === 359.U) {
      angle := 0.U
    }
  }

  val diffInfo = RegInit(diffInfos(0))

  val e0 = RegInit(diffInfos(0).e0)
  val e1 = RegInit(diffInfos(0).e1)
  val e2 = RegInit(diffInfos(0).e2)

  val r = RegInit(diffInfos(0).r)
  val g = RegInit(diffInfos(0).g)
  val b = RegInit(diffInfos(0).b)

  val er = RegInit(diffInfos(0).er)
  val eg = RegInit(diffInfos(0).eg)
  val eb = RegInit(diffInfos(0).eb)

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    diffInfo := diffInfos(angle)

    e0 := diffInfos(angle).e0
    e1 := diffInfos(angle).e1
    e2 := diffInfos(angle).e2

    r := diffInfos(angle).r
    g := diffInfos(angle).g
    b := diffInfos(angle).b

    er := diffInfos(angle).er
    eg := diffInfos(angle).eg
    eb := diffInfos(angle).eb

    row := 0.U
  }

  val tileWriter = Module(new TileWriter)
  val valid = RegInit(false.B)
  tileWriter.io.inReq.valid := valid
  when (valid && tileWriter.io.inReq.ready) {
    valid := false.B

    e0  := e0 + diffInfo.dc0
    e1  := e1 + diffInfo.dc1
    e2  := e2 + diffInfo.dc2

    val (rquo, rrem) = incrDiv(diffInfo.dquorc, diffInfo.dremrc, diffInfo.a, r, er)
    r   := rquo
    er  := rrem

    val (gquo, grem) = incrDiv(diffInfo.dquogc, diffInfo.dremgc, diffInfo.a, g, eg)
    g   := gquo
    eg  := grem

    val (bquo, brem) = incrDiv(diffInfo.dquobc, diffInfo.drembc, diffInfo.a, b, eb)
    b   := bquo
    eb  := brem

    col := col + 1.U

    when (col === (Tile.nrCols - 1).U) {
      e0  := e0 + diffInfo.dr0
      e1  := e1 + diffInfo.dr1
      e2  := e2 + diffInfo.dr2

      val (rquo, rrem) = incrDiv(diffInfo.dquorr, diffInfo.dremrr, diffInfo.a, r, er)
      r   := rquo
      er  := rrem

      val (gquo, grem) = incrDiv(diffInfo.dquogr, diffInfo.dremgr, diffInfo.a, g, eg)
      g   := gquo
      eg  := grem

      val (bquo, brem) = incrDiv(diffInfo.dquobr, diffInfo.drembr, diffInfo.a, b, eb)
      b   := bquo
      eb  := brem

      col := 0.U
      row := row + 1.U
    }
  }

  val tileBuffer = Reg(Vec(Tile.size, Vec(Tile.size, FbRGB())))
  val i = RegInit(0.U(log2Up(Tile.size).W))
  val j = RegInit(0.U(log2Up(Tile.size).W))
  val visible = e0 > 0.S && (e1 > 0.S && e2 > 0.S) || e0 < 0.S && (e1 < 0.S && e2 < 0.S)
  when (row =/= Tile.nrRows.U && !valid) {
    val rgb = FbRGB(r.asUInt, g.asUInt, b.asUInt)
    tileBuffer(i)(j) := Mux(visible, rgb, FbRGB(0))

    j := j + 1.U

    e0 := e0 + diffInfo.dj0
    e1 := e1 + diffInfo.dj1
    e2 := e2 + diffInfo.dj2

    val (rquo, rrem) = incrDiv(diffInfo.dquorj, diffInfo.dremrj, diffInfo.a, r, er)
    r   := rquo
    er  := rrem

    val (gquo, grem) = incrDiv(diffInfo.dquogj, diffInfo.dremgj, diffInfo.a, g, eg)
    g   := gquo
    eg  := grem

    val (bquo, brem) = incrDiv(diffInfo.dquobj, diffInfo.drembj, diffInfo.a, b, eb)
    b   := bquo
    eb  := brem

    when (j === (Tile.size - 1).U) {
      j := 0.U
      i := i + 1.U

      e0  := e0 + diffInfo.di0
      e1  := e1 + diffInfo.di1
      e2  := e2 + diffInfo.di2

      val (rquo, rrem) = incrDiv(diffInfo.dquori, diffInfo.dremri, diffInfo.a, r, er)
      r   := rquo
      er  := rrem

      val (gquo, grem) = incrDiv(diffInfo.dquogi, diffInfo.dremgi, diffInfo.a, g, eg)
      g   := gquo
      eg  := grem

      val (bquo, brem) = incrDiv(diffInfo.dquobi, diffInfo.drembi, diffInfo.a, b, eb)
      b   := bquo
      eb  := brem

      when (i === (Tile.size - 1).U) {
        i := 0.U
        valid := true.B
      }
    }
  }

  for (i <- 0 until Tile.size) {
    for (j <- 0 until Tile.size) {
      tileWriter.io.inReq.bits(i)(j) := tileBuffer(i)(j)
    }
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req <> tileWriter.io.outReq
  io.done := row === Tile.nrRows.U && fbWriter.io.done
}
