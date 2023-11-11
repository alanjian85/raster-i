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


  val e0v = Wire(Vec(360, SInt(32.W)))
  val e1v = Wire(Vec(360, SInt(32.W)))
  val e2v = Wire(Vec(360, SInt(32.W)))

  val rv = Wire(Vec(360, SInt(32.W)))
  val gv = Wire(Vec(360, SInt(32.W)))
  val bv = Wire(Vec(360, SInt(32.W)))

  val erv = Wire(Vec(360, SInt(32.W)))
  val egv = Wire(Vec(360, SInt(32.W)))
  val ebv = Wire(Vec(360, SInt(32.W)))

  var as = Seq[SInt]()

  var dj0s = Seq[SInt]()
  var dj1s = Seq[SInt]()
  var dj2s = Seq[SInt]()

  var di0s = Seq[SInt]()
  var di1s = Seq[SInt]()
  var di2s = Seq[SInt]()

  var dc0s = Seq[SInt]()
  var dc1s = Seq[SInt]()
  var dc2s = Seq[SInt]()

  var dr0s = Seq[SInt]()
  var dr1s = Seq[SInt]()
  var dr2s = Seq[SInt]()

  var dquorjs = Seq[SInt]()
  var dquogjs = Seq[SInt]()
  var dquobjs = Seq[SInt]()

  var dremrjs = Seq[SInt]()
  var dremgjs = Seq[SInt]()
  var drembjs = Seq[SInt]()

  var dquoris = Seq[SInt]()
  var dquogis = Seq[SInt]()
  var dquobis = Seq[SInt]()

  var dremris = Seq[SInt]()
  var dremgis = Seq[SInt]()
  var drembis = Seq[SInt]()

  var dquorcs = Seq[SInt]()
  var dquogcs = Seq[SInt]()
  var dquobcs = Seq[SInt]()

  var dremrcs = Seq[SInt]()
  var dremgcs = Seq[SInt]()
  var drembcs = Seq[SInt]()

  var dquorrs = Seq[SInt]()
  var dquogrs = Seq[SInt]()
  var dquobrs = Seq[SInt]()

  var dremrrs = Seq[SInt]()
  var dremgrs = Seq[SInt]()
  var drembrs = Seq[SInt]()

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

    val dy0 = y0 - y1
    val dy1 = y1 - y2
    val dy2 = y2 - y0

    dj0s :+= -dy0.S
    dj1s :+= -dy1.S
    dj2s :+= -dy2.S

    di0s :+= (dy0 * (Tile.size - 1) - dx0).S
    di1s :+= (dy1 * (Tile.size - 1) - dx1).S
    di2s :+= (dy2 * (Tile.size - 1) - dx2).S

    dc0s :+= ((-dy0 + dx0) * Tile.size).S
    dc1s :+= ((-dy1 + dx1) * Tile.size).S
    dc2s :+= ((-dy2 + dx2) * Tile.size).S

    dr0s :+= (dy0 * (Tile.nrCols - 1) * Tile.size).S
    dr1s :+= (dy1 * (Tile.nrCols - 1) * Tile.size).S
    dr2s :+= (dy2 * (Tile.nrCols - 1) * Tile.size).S

    val e0 = dx0 * y0 + dy0 * x0
    val e1 = dx1 * y1 + dy1 * x1
    val e2 = dx2 * y2 + dy2 * x2
    e0v(i) := e0.S
    e1v(i) := e1.S
    e2v(i) := e2.S

    val a = dy0 * dx2 - dx0 * dy2
    as :+= math.abs(a).S

    val r = if (a == 0) 0 else e1 * 255 / a
    val g = if (a == 0) 0 else e2 * 255 / a
    val b = if (a == 0) 0 else e0 * 255 / a
    rv(i) := r.S
    gv(i) := g.S
    bv(i) := b.S

    val er = if (a == 0) 0 else if (a > 0) e1 * 255 % a else -e1 * 255 % a
    val eg = if (a == 0) 0 else if (a > 0) e2 * 255 % a else -e2 * 255 % a
    val eb = if (a == 0) 0 else if (a > 0) e0 * 255 % a else -e0 * 255 % a
    erv(i) := er.S
    egv(i) := eg.S
    ebv(i) := eb.S

    val dividendrj = -dy1 * 255
    val dividendgj = -dy2 * 255
    val dividendbj = -dy0 * 255

    dquorjs :+= (if (a == 0) 0 else dividendrj / a).S
    dquogjs :+= (if (a == 0) 0 else dividendgj / a).S
    dquobjs :+= (if (a == 0) 0 else dividendbj / a).S

    dremrjs :+= (if (a == 0) 0 else if (a > 0) dividendrj % a else -dividendrj % a).S
    dremgjs :+= (if (a == 0) 0 else if (a > 0) dividendgj % a else -dividendgj % a).S
    drembjs :+= (if (a == 0) 0 else if (a > 0) dividendbj % a else -dividendbj % a).S

    val dividendri = (dy1 * (Tile.size - 1) - dx1) * 255
    val dividendgi = (dy2 * (Tile.size - 1) - dx2) * 255
    val dividendbi = (dy0 * (Tile.size - 1) - dx0) * 255

    dquoris :+= (if (a == 0) 0 else dividendri / a).S
    dquogis :+= (if (a == 0) 0 else dividendgi / a).S
    dquobis :+= (if (a == 0) 0 else dividendbi / a).S

    dremris :+= (if (a == 0) 0 else if (a > 0) dividendri % a else -dividendri % a).S
    dremgis :+= (if (a == 0) 0 else if (a > 0) dividendgi % a else -dividendgi % a).S
    drembis :+= (if (a == 0) 0 else if (a > 0) dividendbi % a else -dividendbi % a).S

    val dividendrc = (-dy1 + dx1) * Tile.size * 255
    val dividendgc = (-dy2 + dx2) * Tile.size * 255
    val dividendbc = (-dy0 + dx0) * Tile.size * 255

    dquorcs :+= (if (a == 0) 0 else dividendrc / a).S
    dquogcs :+= (if (a == 0) 0 else dividendgc / a).S
    dquobcs :+= (if (a == 0) 0 else dividendbc / a).S

    dremrcs :+= (if (a == 0) 0 else if (a > 0) dividendrc % a else -dividendrc % a).S
    dremgcs :+= (if (a == 0) 0 else if (a > 0) dividendgc % a else -dividendgc % a).S
    drembcs :+= (if (a == 0) 0 else if (a > 0) dividendbc % a else -dividendbc % a).S

    val dividendrr = dy1 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendgr = dy2 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendbr = dy0 * (Tile.nrCols - 1) * Tile.size * 255

    dquorrs :+= (if (a == 0) 0 else dividendrr / a).S
    dquogrs :+= (if (a == 0) 0 else dividendgr / a).S
    dquobrs :+= (if (a == 0) 0 else dividendbr / a).S

    dremrrs :+= (if (a == 0) 0 else if (a > 0) dividendrr % a else -dividendrr % a).S
    dremgrs :+= (if (a == 0) 0 else if (a > 0) dividendgr % a else -dividendgr % a).S
    drembrs :+= (if (a == 0) 0 else if (a > 0) dividendbr % a else -dividendbr % a).S
  }

  val av = VecInit(as)

  val dj0v = VecInit(dj0s)
  val dj1v = VecInit(dj1s)
  val dj2v = VecInit(dj2s)

  val di0v = VecInit(di0s)
  val di1v = VecInit(di1s)
  val di2v = VecInit(di2s)

  val dc0v = VecInit(dc0s)
  val dc1v = VecInit(dc1s)
  val dc2v = VecInit(dc2s)

  val dr0v = VecInit(dr0s)
  val dr1v = VecInit(dr1s)
  val dr2v = VecInit(dr2s)

  val dquorjv = VecInit(dquorjs)
  val dquogjv = VecInit(dquogjs)
  val dquobjv = VecInit(dquobjs)

  val dremrjv = VecInit(dremrjs)
  val dremgjv = VecInit(dremgjs)
  val drembjv = VecInit(drembjs)

  val dquoriv = VecInit(dquoris)
  val dquogiv = VecInit(dquogis)
  val dquobiv = VecInit(dquobis)

  val dremriv = VecInit(dremris)
  val dremgiv = VecInit(dremgis)
  val drembiv = VecInit(drembis)

  val dquorcv = VecInit(dquorcs)
  val dquogcv = VecInit(dquogcs)
  val dquobcv = VecInit(dquobcs)

  val dremrcv = VecInit(dremrcs)
  val dremgcv = VecInit(dremgcs)
  val drembcv = VecInit(drembcs)

  val dquorrv = VecInit(dquorrs)
  val dquogrv = VecInit(dquogrs)
  val dquobrv = VecInit(dquobrs)

  val dremrrv = VecInit(dremrrs)
  val dremgrv = VecInit(dremgrs)
  val drembrv = VecInit(drembrs)

  val currAngle = RegInit(0.U(log2Up(360).W))
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

  val e0 = RegInit(e0v(0))
  val e1 = RegInit(e1v(0))
  val e2 = RegInit(e2v(0))

  val r = RegInit(rv(0))
  val g = RegInit(gv(0))
  val b = RegInit(bv(0))

  val er = RegInit(erv(0))
  val eg = RegInit(egv(0))
  val eb = RegInit(ebv(0))

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    currAngle := angle

    e0 := e0v(angle)
    e1 := e1v(angle)
    e2 := e2v(angle)

    r := rv(angle)
    g := gv(angle)
    b := bv(angle)

    er := erv(angle)
    eg := egv(angle)
    eb := ebv(angle)

    row := 0.U
  }

  val tileWriter = Module(new TileWriter)
  val valid = RegInit(false.B)
  tileWriter.io.inReq.valid := valid
  when (valid && tileWriter.io.inReq.ready) {
    valid := false.B

    e0  := e0 + dc0v(currAngle)
    e1  := e1 + dc1v(currAngle)
    e2  := e2 + dc2v(currAngle)

    val (rquo, rrem) = incrDiv(dquorcv(currAngle), dremrcv(currAngle), av(currAngle), r, er)
    r   := rquo
    er  := rrem

    val (gquo, grem) = incrDiv(dquogcv(currAngle), dremgcv(currAngle), av(currAngle), g, eg)
    g   := gquo
    eg  := grem

    val (bquo, brem) = incrDiv(dquobcv(currAngle), drembcv(currAngle), av(currAngle), b, eb)
    b   := bquo
    eb  := brem

    col := col + 1.U

    when (col === (Tile.nrCols - 1).U) {
      e0  := e0 + dr0v(currAngle)
      e1  := e1 + dr1v(currAngle)
      e2  := e2 + dr2v(currAngle)

      val (rquo, rrem) = incrDiv(dquorrv(currAngle), dremrrv(currAngle), av(currAngle), r, er)
      r   := rquo
      er  := rrem

      val (gquo, grem) = incrDiv(dquogrv(currAngle), dremgrv(currAngle), av(currAngle), g, eg)
      g   := gquo
      eg  := grem

      val (bquo, brem) = incrDiv(dquobrv(currAngle), drembrv(currAngle), av(currAngle), b, eb)
      b   := bquo
      eb  := brem

      col := 0.U
      row := row + 1.U
    }
  }

  val tileBuffer = Reg(Vec(Tile.size, Vec(Tile.size, FbRGB())))
  val i = RegInit(0.U(log2Up(Tile.size).W))
  val j = RegInit(0.U(log2Up(Tile.size).W))
  when (row =/= Tile.nrRows.U && !valid) {
    val rgb = FbRGB(r.asUInt, g.asUInt, b.asUInt)
    val visible = e0 > 0.S && (e1 > 0.S && e2 > 0.S) || e0 < 0.S && (e1 < 0.S && e2 < 0.S)
    tileBuffer(i)(j) := Mux(visible, rgb, FbRGB(0))

    j := j + 1.U

    e0 := e0 + dj0v(currAngle)
    e1 := e1 + dj1v(currAngle)
    e2 := e2 + dj2v(currAngle)

    val (rquo, rrem) = incrDiv(dquorjv(currAngle), dremrjv(currAngle), av(currAngle), r, er)
    r   := rquo
    er  := rrem

    val (gquo, grem) = incrDiv(dquogjv(currAngle), dremgjv(currAngle), av(currAngle), g, eg)
    g   := gquo
    eg  := grem

    val (bquo, brem) = incrDiv(dquobjv(currAngle), drembjv(currAngle), av(currAngle), b, eb)
    b   := bquo
    eb  := brem

    when (j === (Tile.size - 1).U) {
      j := 0.U
      i := i + 1.U

      e0 := e0 + di0v(currAngle)
      e1 := e1 + di1v(currAngle)
      e2 := e2 + di2v(currAngle)

      val (rquo, rrem) = incrDiv(dquoriv(currAngle), dremriv(currAngle), av(currAngle), r, er)
      r   := rquo
      er  := rrem

      val (gquo, grem) = incrDiv(dquogiv(currAngle), dremgiv(currAngle), av(currAngle), g, eg)
      g   := gquo
      eg  := grem

      val (bquo, brem) = incrDiv(dquobiv(currAngle), drembiv(currAngle), av(currAngle), b, eb)
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
  io.done := fbWriter.io.done
}
