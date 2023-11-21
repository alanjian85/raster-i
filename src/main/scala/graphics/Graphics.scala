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

  val cnt = RegInit(0.U(unsignedBitLength(1388888).W))
  val angle = RegInit(0.U(log2Up(360).W))
  cnt := cnt + 1.U
  when (cnt === 1388888.U) {
    cnt := 0.U
    angle := angle + 1.U
    when (angle === 359.U) {
      angle := 0.U
    }
  }

  val nrDraws = 3
  val drawId = RegInit(0.U(log2Up(nrDraws).W))

  val r = Wire(Vec(nrDraws, UInt(8.W)))
  val g = Wire(Vec(nrDraws, UInt(8.W)))
  val b = Wire(Vec(nrDraws, UInt(8.W)))

  val x0v = Wire(Vec(nrDraws, Vec(360, UInt())))
  val y0v = Wire(Vec(nrDraws, Vec(360, UInt())))
  val x1v = Wire(Vec(nrDraws, Vec(360, UInt())))
  val y1v = Wire(Vec(nrDraws, Vec(360, UInt())))
  val x2v = Wire(Vec(nrDraws, Vec(360, UInt())))
  val y2v = Wire(Vec(nrDraws, Vec(360, UInt())))

  val dx0v = Wire(Vec(nrDraws, Vec(360, SInt())))
  val dy0v = Wire(Vec(nrDraws, Vec(360, SInt())))
  val dx1v = Wire(Vec(nrDraws, Vec(360, SInt())))
  val dy1v = Wire(Vec(nrDraws, Vec(360, SInt())))
  val dx2v = Wire(Vec(nrDraws, Vec(360, SInt())))
  val dy2v = Wire(Vec(nrDraws, Vec(360, SInt())))
  for (i <- 0 until 360) {
    r(0) := 0.U
    g(0) := 0.U
    b(0) := 0.U

    x0v(0)(i) := 0.U
    y0v(0)(i) := 0.U
    x1v(0)(i) := 0.U
    y1v(0)(i) := 0.U
    x2v(0)(i) := 0.U
    y2v(0)(i) := 0.U

    dx0v(0)(i) := 0.S
    dx1v(0)(i) := 0.S
    dx2v(0)(i) := 0.S
    dy0v(0)(i) := 0.S
    dy1v(0)(i) := 0.S
    dy2v(0)(i) := 0.S

    val angle = math.toRadians(i)
    val x0 = 512
    val y0 = 192
    val z1 = 1 / (2 - math.sin(angle))
    val x1 = 512 - (222 * z1 * math.cos(angle)).toInt
    val y1 = 384 + (192 * z1).toInt
    val z2 = 1 / (2 + math.sin(angle))
    val x2 = 512 + (222 * z2 * math.cos(angle)).toInt
    val y2 = 384 + (192 * z2).toInt

    r(1) := 255.U
    g(1) := 0.U
    b(1) := 0.U

    x0v(1)(i) := (x0 - 200).U
    y0v(1)(i) := y0.U
    x1v(1)(i) := (x1 - 200).U
    y1v(1)(i) := y1.U
    x2v(1)(i) := (x2 - 200).U
    y2v(1)(i) := y2.U

    dx0v(1)(i) := (x1 - x0).S
    dx1v(1)(i) := (x2 - x1).S
    dx2v(1)(i) := (x0 - x2).S
    dy0v(1)(i) := (y0 - y1).S
    dy1v(1)(i) := (y1 - y2).S
    dy2v(1)(i) := (y2 - y0).S

    r(2) := 0.U
    g(2) := 255.U
    b(2) := 0.U

    x0v(2)(i) := (x0 + 200).U
    y0v(2)(i) := y0.U
    x1v(2)(i) := (x1 + 200).U
    y1v(2)(i) := y1.U
    x2v(2)(i) := (x2 + 200).U
    y2v(2)(i) := y2.U

    dx0v(2)(i) := (x1 - x0).S
    dx1v(2)(i) := (x2 - x1).S
    dx2v(2)(i) := (x0 - x2).S
    dy0v(2)(i) := (y0 - y1).S
    dy1v(2)(i) := (y1 - y2).S
    dy2v(2)(i) := (y2 - y0).S
  }

  val x0 = RegInit(x0v(0)(0))
  val x1 = RegInit(x1v(0)(0))
  val x2 = RegInit(x2v(0)(0))
  val y0 = RegInit(y0v(0)(0))
  val y1 = RegInit(y1v(0)(0))
  val y2 = RegInit(y2v(0)(0))

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  val x = RegInit(0.U(log2Up(Fb.width).W))
  val y = RegInit(0.U(log2Up(Fb.height).W))
  val currAngle = RegInit(0.U(log2Up(360).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    drawId := 0.U
    x0 := x0v(0)(angle)
    x1 := x1v(0)(angle)
    x2 := x2v(0)(angle)
    y0 := y0v(0)(angle)
    y1 := y1v(0)(angle)
    y2 := y2v(0)(angle)
    currAngle := angle
    row := 0.U
    x := 0.U
    y := 0.U
  }

  val tileWriter = Module(new TileWriter)
  val valid = RegInit(false.B)
  tileWriter.io.inReq.valid := valid
  when (valid && tileWriter.io.inReq.ready) {
    valid := false.B
    col := col + 1.U
    x := x + Tile.size.U
    when (col === (Tile.nrCols - 1).U) {
      col := 0.U
      row := row + 1.U
      x := 0.U
      y := y + Tile.size.U
    }
  }

  val tileBuffer = Reg(Vec(Tile.size, Vec(Tile.size, FbRGB())))
  val i = RegInit(0.U(log2Up(Tile.size).W))
  val j = RegInit(0.U(log2Up(Tile.size).W))
  when (row =/= Tile.nrRows.U && !valid) {
    val e0 = dx0v(drawId)(currAngle) * (y0.zext - y.zext) - dy0v(drawId)(currAngle) * (x.zext - x0.zext)
    val e1 = dx1v(drawId)(currAngle) * (y1.zext - y.zext) - dy1v(drawId)(currAngle) * (x.zext - x1.zext)
    val e2 = dx2v(drawId)(currAngle) * (y2.zext - y.zext) - dy2v(drawId)(currAngle) * (x.zext - x2.zext)
    val visible = e0 > 0.S && e1 > 0.S && e2 > 0.S ||
      e0 < 0.S && e1 < 0.S && e2 < 0.S ||
      e0 === 0.S && e1 === 0.S && e2 === 0.S
    when (visible) {
      tileBuffer(i)(j) := FbRGB(r(drawId), g(drawId), b(drawId))
    }

    j := j + 1.U
    x := x + 1.U
    when (j === (Tile.size - 1).U) {
      j := 0.U
      i := i + 1.U
      x := x - (Tile.size - 1).U
      y := y + 1.U
      when (i === (Tile.size - 1).U) {
        val ndrawId = WireDefault(drawId + 1.U)
        x0 := x0v(ndrawId)(currAngle)
        x1 := x1v(ndrawId)(currAngle)
        x2 := x2v(ndrawId)(currAngle)
        y0 := y0v(ndrawId)(currAngle)
        y1 := y1v(ndrawId)(currAngle)
        y2 := y2v(ndrawId)(currAngle)
        i := 0.U
        y := y - (Tile.size - 1).U
        drawId := ndrawId
        when (drawId === (nrDraws - 1).U) {
          ndrawId := 0.U
          valid := true.B
        }
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
