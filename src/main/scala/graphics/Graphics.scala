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

  val dx0v = Wire(Vec(360, SInt(signedBitLength(222).W)))
  val dx1v = Wire(Vec(360, SInt(signedBitLength(444).W)))
  val dx2v = Wire(Vec(360, SInt(signedBitLength(222).W)))

  val dy0v = Wire(Vec(360, SInt(signedBitLength(384).W)))
  val dy1v = Wire(Vec(360, SInt(signedBitLength(192).W)))
  val dy2v = Wire(Vec(360, SInt(signedBitLength(384).W)))

  val e0v = Wire(Vec(360, SInt(32.W)))
  val e1v = Wire(Vec(360, SInt(32.W)))
  val e2v = Wire(Vec(360, SInt(32.W)))
/*
  val av = Wire(Vec(360, SInt()))

  val rv = Wire(Vec(360, SInt(32.W)))
  val gv = Wire(Vec(360, SInt(32.W)))
  val bv = Wire(Vec(360, SInt(32.W)))

  val erv = Wire(Vec(360, SInt(32.W)))
  val egv = Wire(Vec(360, SInt(32.W)))
  val ebv = Wire(Vec(360, SInt(32.W)))

  val dquorv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))
  val dquogv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))
  val dquobv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))

  val dremrv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))
  val dremgv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))
  val drembv = Wire(Vec(360, Vec(Tile.size + 1, Vec(Tile.size + 1, SInt()))))
 */
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

    val dx0 = x1 - x0
    val dx1 = x2 - x1
    val dx2 = x0 - x2
    dx0v(i) := dx0.S
    dx1v(i) := dx1.S
    dx2v(i) := dx2.S

    val dy0 = y0 - y1
    val dy1 = y1 - y2
    val dy2 = y2 - y0
    dy0v(i) := dy0.S
    dy1v(i) := dy1.S
    dy2v(i) := dy2.S

    val e0 = dx0 * y0 + dy0 * x0
    val e1 = dx1 * y1 + dy1 * x1
    val e2 = dx2 * y2 + dy2 * x2
    e0v(i) := e0.S
    e1v(i) := e1.S
    e2v(i) := e2.S
/*
    val a = dy0 * dx2 - dx0 * dy2
    av(i) := a.S

    val r = if (a == 0) 0 else e1 * 255 / a
    val g = if (a == 0) 0 else e2 * 255 / a
    val b = if (a == 0) 0 else e0 * 255 / a
    rv(i) := r.S
    gv(i) := g.S
    bv(i) := b.S

    val er = if (a == 0) 0 else e1 * 255 % a
    val eg = if (a == 0) 0 else e2 * 255 % a
    val eb = if (a == 0) 0 else e0 * 255 % a
    erv(i) := er.S
    egv(i) := eg.S
    ebv(i) := eb.S

    for (j <- 0 to Tile.size) {
      for (k <- 0 to Tile.size) {
        val dividendr = -(dx1 * j + dy1 * k) * 255
        val dividendg = -(dx2 * j + dy2 * k) * 255
        val dividendb = -(dx0 * j + dy0 * k) * 255

        dquorv(i)(j)(k) := (if (a == 0) 0 else dividendr / a).S
        dquogv(i)(j)(k) := (if (a == 0) 0 else dividendg / a).S
        dquobv(i)(j)(k) := (if (a == 0) 0 else dividendb / a).S

        dremrv(i)(j)(k) := (if (a == 0) 0 else dividendr % a).S
        dremgv(i)(j)(k) := (if (a == 0) 0 else dividendg % a).S
        drembv(i)(j)(k) := (if (a == 0) 0 else dividendb % a).S
      }
    }
 */
  }

  val currAngle = RegInit(0.U(log2Up(360).W))

  val e0 = RegInit(e0v(0))
  val e1 = RegInit(e1v(0))
  val e2 = RegInit(e2v(0))

  val pe0 = RegInit(e0v(0))
  val pe1 = RegInit(e1v(0))
  val pe2 = RegInit(e2v(0))
/*
  val r = RegInit(rv(0))
  val g = RegInit(gv(0))
  val b = RegInit(bv(0))

  val pr = RegInit(rv(0))
  val pg = RegInit(gv(0))
  val pb = RegInit(bv(0))

  val er = RegInit(erv(0))
  val eg = RegInit(egv(0))
  val eb = RegInit(ebv(0))

  val per = RegInit(erv(0))
  val peg = RegInit(egv(0))
  val peb = RegInit(ebv(0))
 */
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

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    currAngle := angle

    e0  := e0v(angle)
    e1  := e1v(angle)
    e2  := e2v(angle)

    pe0 := e0v(angle)
    pe1 := e1v(angle)
    pe2 := e2v(angle)
/*
    r   := rv(angle)
    g   := gv(angle)
    b   := bv(angle)

    pr  := rv(angle)
    pg  := gv(angle)
    pb  := bv(angle)

    er  := erv(angle)
    eg  := egv(angle)
    eb  := ebv(angle)

    per := erv(angle)
    peg := egv(angle)
    peb := ebv(angle)
 */
    col := 0.U
    row := 0.U
  }

  val tileBuffer = Module(new TileBuffer)
  val valid = row < Tile.nrRows.U
  tileBuffer.io.inReq.valid := valid
  when (valid && tileBuffer.io.inReq.ready) {
    e0  := e0 - dy0v(currAngle) * Tile.size.S
    e1  := e1 - dy1v(currAngle) * Tile.size.S
    e2  := e2 - dy2v(currAngle) * Tile.size.S
    /*
    val (rquo, rrem) = incrDiv(dquorv(currAngle)(0)(Tile.size), dremrv(currAngle)(0)(Tile.size), av(currAngle), r, er)
    r  := rquo
    er := rrem
    val (gquo, grem) = incrDiv(dquogv(currAngle)(0)(Tile.size), dremgv(currAngle)(0)(Tile.size), av(currAngle), g, eg)
    g  := gquo
    eg := grem
    val (bquo, brem) = incrDiv(dquobv(currAngle)(0)(Tile.size), drembv(currAngle)(0)(Tile.size), av(currAngle), b, eb)
    b  := bquo
    eb := brem
     */
    col := col + 1.U
    when (col === (Tile.nrCols - 1).U) {
      val ne0 = pe0 - dx0v(currAngle) * Tile.size.S
      val ne1 = pe1 - dx1v(currAngle) * Tile.size.S
      val ne2 = pe2 - dx2v(currAngle) * Tile.size.S
      e0  := ne0
      pe0 := ne0
      e1  := ne1
      pe1 := ne1
      e2  := ne2
      pe2 := ne2
/*
      val (rquo, rrem) = incrDiv(dquorv(currAngle)(Tile.size)(0), dremrv(currAngle)(Tile.size)(0), av(currAngle), pr, per)
      r   := rquo
      er  := rrem
      pr  := rquo
      per := rrem

      val (gquo, grem) = incrDiv(dquogv(currAngle)(Tile.size)(0), dremgv(currAngle)(Tile.size)(0), av(currAngle), pg, peg)
      g   := gquo
      eg  := grem
      pg  := gquo
      peg := grem

      val (bquo, brem) = incrDiv(dquobv(currAngle)(Tile.size)(0), drembv(currAngle)(Tile.size)(0), av(currAngle), pb, peb)
      b   := bquo
      eb  := brem
      pb  := bquo
      peb := brem
 */
      col := 0.U
      row := row + 1.U
    }
  }


  for (i <- 0 until Tile.size) {
    val pe0 = e0 - dx0v(currAngle) * i.S
    val pe1 = e1 - dx1v(currAngle) * i.S
    val pe2 = e2 - dx2v(currAngle) * i.S
    val visible = pe0 > 0.S && (pe1 > 0.S && pe2 > 0.S) || pe0 < 0.S && (pe1 < 0.S && pe2 < 0.S)
    for (j <- 0 until Tile.size) {
      //val (rquo, _) = incrDiv(dquorv(currAngle)(i)(j), dremrv(currAngle)(i)(j), av(currAngle), r, er)
      //val (gquo, _) = incrDiv(dquogv(currAngle)(i)(j), dremgv(currAngle)(i)(j), av(currAngle), g, eg)
      //val (bquo, _) = incrDiv(dquobv(currAngle)(i)(j), drembv(currAngle)(i)(j), av(currAngle), b, eb)
      tileBuffer.io.inReq.bits(i)(j).r := Mux(visible, 255.U, 0.U)//Mux(visible, rquo.asUInt, 0.U)
      tileBuffer.io.inReq.bits(i)(j).g := Mux(visible, 255.U, 0.U)//Mux(visible, gquo.asUInt, 0.U)
      tileBuffer.io.inReq.bits(i)(j).b := Mux(visible, 255.U, 0.U)//Mux(visible, bquo.asUInt, 0.U)
    }
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req <> tileBuffer.io.outReq
  io.done := !valid && fbWriter.io.done
}
