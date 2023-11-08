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

  val av = Wire(Vec(360, SInt(32.W)))

  val rv = Wire(Vec(360, SInt(32.W)))
  val gv = Wire(Vec(360, SInt(32.W)))
  val bv = Wire(Vec(360, SInt(32.W)))

  val erv = Wire(Vec(360, SInt(32.W)))
  val egv = Wire(Vec(360, SInt(32.W)))
  val ebv = Wire(Vec(360, SInt(32.W)))

  val dquorjv = Wire(Vec(360, SInt(32.W)))
  val dquogjv = Wire(Vec(360, SInt(32.W)))
  val dquobjv = Wire(Vec(360, SInt(32.W)))

  val dremrjv = Wire(Vec(360, SInt(32.W)))
  val dremgjv = Wire(Vec(360, SInt(32.W)))
  val drembjv = Wire(Vec(360, SInt(32.W)))

  val dquoriv = Wire(Vec(360, SInt(32.W)))
  val dquogiv = Wire(Vec(360, SInt(32.W)))
  val dquobiv = Wire(Vec(360, SInt(32.W)))

  val dremriv = Wire(Vec(360, SInt(32.W)))
  val dremgiv = Wire(Vec(360, SInt(32.W)))
  val drembiv = Wire(Vec(360, SInt(32.W)))

  val dquorcv = Wire(Vec(360, SInt(32.W)))
  val dquogcv = Wire(Vec(360, SInt(32.W)))
  val dquobcv = Wire(Vec(360, SInt(32.W)))

  val dremrcv = Wire(Vec(360, SInt(32.W)))
  val dremgcv = Wire(Vec(360, SInt(32.W)))
  val drembcv = Wire(Vec(360, SInt(32.W)))

  val dquorrv = Wire(Vec(360, SInt(32.W)))
  val dquogrv = Wire(Vec(360, SInt(32.W)))
  val dquobrv = Wire(Vec(360, SInt(32.W)))

  val dremrrv = Wire(Vec(360, SInt(32.W)))
  val dremgrv = Wire(Vec(360, SInt(32.W)))
  val drembrv = Wire(Vec(360, SInt(32.W)))

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

    val a = dy0 * dx2 - dx0 * dy2
    av(i) := math.abs(a).S

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

    dquorjv(i) := (if (a == 0) 0 else dividendrj / a).S
    dquogjv(i) := (if (a == 0) 0 else dividendgj / a).S
    dquobjv(i) := (if (a == 0) 0 else dividendbj / a).S

    dremrjv(i) := (if (a == 0) 0 else if (a > 0) dividendrj % a else -dividendrj % a).S
    dremgjv(i) := (if (a == 0) 0 else if (a > 0) dividendgj % a else -dividendgj % a).S
    drembjv(i) := (if (a == 0) 0 else if (a > 0) dividendbj % a else -dividendbj % a).S

    val dividendri = -dx1 * 255
    val dividendgi = -dx2 * 255
    val dividendbi = -dx0 * 255

    dquoriv(i) := (if (a == 0) 0 else dividendri / a).S
    dquogiv(i) := (if (a == 0) 0 else dividendgi / a).S
    dquobiv(i) := (if (a == 0) 0 else dividendbi / a).S

    dremriv(i) := (if (a == 0) 0 else if (a > 0) dividendri % a else -dividendri % a).S
    dremgiv(i) := (if (a == 0) 0 else if (a > 0) dividendgi % a else -dividendgi % a).S
    drembiv(i) := (if (a == 0) 0 else if (a > 0) dividendbi % a else -dividendbi % a).S

    val dividendrc = -dy1 * Tile.size * 255
    val dividendgc = -dy2 * Tile.size * 255
    val dividendbc = -dy0 * Tile.size * 255

    dquorcv(i) := (if (a == 0) 0 else dividendrc / a).S
    dquogcv(i) := (if (a == 0) 0 else dividendgc / a).S
    dquobcv(i) := (if (a == 0) 0 else dividendbc / a).S

    dremrcv(i) := (if (a == 0) 0 else if (a > 0) dividendrc % a else -dividendrc % a).S
    dremgcv(i) := (if (a == 0) 0 else if (a > 0) dividendgc % a else -dividendgc % a).S
    drembcv(i) := (if (a == 0) 0 else if (a > 0) dividendbc % a else -dividendbc % a).S

    val dividendrr = -dx1 * Tile.size * 255
    val dividendgr = -dx2 * Tile.size * 255
    val dividendbr = -dx0 * Tile.size * 255

    dquorrv(i) := (if (a == 0) 0 else dividendrr / a).S
    dquogrv(i) := (if (a == 0) 0 else dividendgr / a).S
    dquobrv(i) := (if (a == 0) 0 else dividendbr / a).S

    dremrrv(i) := (if (a == 0) 0 else if (a > 0) dividendrr % a else -dividendrr % a).S
    dremgrv(i) := (if (a == 0) 0 else if (a > 0) dividendgr % a else -dividendgr % a).S
    drembrv(i) := (if (a == 0) 0 else if (a > 0) dividendbr % a else -dividendbr % a).S
  }

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
  val ie0 = RegInit(e0v(0))
  val ie1 = RegInit(e1v(0))
  val ie2 = RegInit(e2v(0))
  val ce0 = RegInit(e0v(0))
  val ce1 = RegInit(e1v(0))
  val ce2 = RegInit(e2v(0))
  val re0 = RegInit(e0v(0))
  val re1 = RegInit(e1v(0))
  val re2 = RegInit(e2v(0))

  val r = RegInit(rv(0))
  val g = RegInit(gv(0))
  val b = RegInit(bv(0))
  val ir = RegInit(rv(0))
  val ig = RegInit(gv(0))
  val ib = RegInit(bv(0))
  val cr = RegInit(rv(0))
  val cg = RegInit(gv(0))
  val cb = RegInit(bv(0))
  val rr = RegInit(rv(0))
  val rg = RegInit(gv(0))
  val rb = RegInit(bv(0))

  val er = RegInit(erv(0))
  val eg = RegInit(egv(0))
  val eb = RegInit(ebv(0))
  val ier = RegInit(erv(0))
  val ieg = RegInit(egv(0))
  val ieb = RegInit(ebv(0))
  val cer = RegInit(erv(0))
  val ceg = RegInit(egv(0))
  val ceb = RegInit(ebv(0))
  val rer = RegInit(erv(0))
  val reg = RegInit(egv(0))
  val reb = RegInit(ebv(0))

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    currAngle := angle

    e0 := e0v(angle)
    e1 := e1v(angle)
    e2 := e2v(angle)
    ie0 := e0v(angle)
    ie1 := e1v(angle)
    ie2 := e2v(angle)
    ce0 := e0v(angle)
    ce1 := e1v(angle)
    ce2 := e2v(angle)
    re0 := e0v(angle)
    re1 := e1v(angle)
    re2 := e2v(angle)

    r := rv(angle)
    g := gv(angle)
    b := bv(angle)
    ir := rv(angle)
    ig := gv(angle)
    ib := bv(angle)
    cr := rv(angle)
    cg := gv(angle)
    cb := bv(angle)
    rr := rv(angle)
    rg := gv(angle)
    rb := bv(angle)

    er := erv(angle)
    eg := egv(angle)
    eb := ebv(angle)
    ier := erv(angle)
    ieg := egv(angle)
    ieb := ebv(angle)
    cer := erv(angle)
    ceg := egv(angle)
    ceb := ebv(angle)
    rer := erv(angle)
    reg := egv(angle)
    reb := ebv(angle)

    col := 0.U
    row := 0.U
  }

  val tileWriter = Module(new TileWriter)
  val valid = RegInit(false.B)
  tileWriter.io.inReq.valid := valid
  when (valid && tileWriter.io.inReq.ready) {
    valid := false.B

    val ne0 = ce0 - dy0v(currAngle) * Tile.size.S
    e0  := ne0
    ie0 := ne0
    ce0 := ne0

    val ne1 = ce1 - dy1v(currAngle) * Tile.size.S
    e1  := ne1
    ie1 := ne1
    ce1 := ne1

    val ne2 = ce2 - dy2v(currAngle) * Tile.size.S
    e2  := ne2
    ie2 := ne2
    ce2 := ne2

    val (rquo, rrem) = incrDiv(dquorcv(currAngle), dremrcv(currAngle), av(currAngle), cr, cer)
    r   := rquo
    er  := rrem
    ir  := rquo
    ier := rrem
    cr  := rquo
    ceg := rrem

    val (gquo, grem) = incrDiv(dquogcv(currAngle), dremgcv(currAngle), av(currAngle), cg, ceg)
    g   := gquo
    eg  := grem
    ig  := gquo
    ieg := grem
    cg  := gquo
    ceg := grem

    val (bquo, brem) = incrDiv(dquobcv(currAngle), drembcv(currAngle), av(currAngle), cb, ceb)
    b   := bquo
    eb  := brem
    ib  := bquo
    ieb := brem
    cb  := bquo
    ceb := brem

    col := col + 1.U

    when (col === (Tile.nrCols - 1).U) {
      val ne0 = re0 - dx0v(currAngle) * Tile.size.S
      e0  := ne0
      ie0 := ne0
      ce0 := ne0
      re0 := ne0

      val ne1 = re1 - dx1v(currAngle) * Tile.size.S
      e1  := ne1
      ie1 := ne1
      ce1 := ne1
      re1 := ne1

      val ne2 = re2 - dx2v(currAngle) * Tile.size.S
      e2  := ne2
      ie2 := ne2
      ce2 := ne2
      re2 := ne2

      val (rquo, rrem) = incrDiv(dquorrv(currAngle), dremrrv(currAngle), av(currAngle), rr, rer)
      r   := rquo
      er  := rrem
      ir  := rquo
      ier := rrem
      cr  := rquo
      cer := rrem
      rr  := rquo
      rer := rrem

      val (gquo, grem) = incrDiv(dquogrv(currAngle), dremgrv(currAngle), av(currAngle), rg, reg)
      g   := gquo
      eg  := grem
      cg  := gquo
      ceg := grem
      rg  := gquo
      reg := grem

      val (bquo, brem) = incrDiv(dquobrv(currAngle), drembrv(currAngle), av(currAngle), rb, reb)
      b   := bquo
      eb  := brem
      cb  := bquo
      ceb := brem
      rb  := bquo
      reb := brem

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
    when (j === (Tile.size - 1).U) {
      j := 0.U
      i := i + 1.U
      when (i === (Tile.size - 1).U) {
        i := 0.U
        valid := true.B
      } .otherwise {
        val ne0 = ie0 - dx0v(currAngle)
        e0  := ne0
        ie0 := ne0
        val ne1 = ie1 - dx1v(currAngle)
        e1  := ne1
        ie1 := ne1
        val ne2 = ie2 - dx2v(currAngle)
        e2  := ne2
        ie2 := ne2

        val (rquo, rrem) = incrDiv(dquoriv(currAngle), dremriv(currAngle), av(currAngle), ir, ier)
        r   := rquo
        er  := rrem
        ir  := rquo
        ier := rrem

        val (gquo, grem) = incrDiv(dquogiv(currAngle), dremgiv(currAngle), av(currAngle), ig, ieg)
        g   := gquo
        eg  := grem
        ig  := gquo
        ieg := grem

        val (bquo, brem) = incrDiv(dquobiv(currAngle), drembiv(currAngle), av(currAngle), ib, ieb)
        b   := bquo
        eb  := brem
        ib  := bquo
        ieb := brem
      }
    } .otherwise {
      e0 := e0 - dy0v(currAngle)
      e1 := e1 - dy1v(currAngle)
      e2 := e2 - dy2v(currAngle)

      val (rquo, rrem) = incrDiv(dquorjv(currAngle), dremrjv(currAngle), av(currAngle), r, er)
      r   := rquo
      er  := rrem

      val (gquo, grem) = incrDiv(dquogjv(currAngle), dremgjv(currAngle), av(currAngle), g, eg)
      g   := gquo
      eg  := grem

      val (bquo, brem) = incrDiv(dquobjv(currAngle), drembjv(currAngle), av(currAngle), b, eb)
      b   := bquo
      eb  := brem
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
