// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class DiffInfo extends Bundle {
  val e0 = SInt(32.W)
  val e1 = SInt(32.W)
  val e2 = SInt(32.W)

  val dx0 = SInt(32.W)
  val dx1 = SInt(32.W)
  val dx2 = SInt(32.W)

  val dy0 = SInt(32.W)
  val dy1 = SInt(32.W)
  val dy2 = SInt(32.W)

  val a = SInt(32.W)

  val r = SInt(32.W)
  val g = SInt(32.W)
  val b = SInt(32.W)

  val er = SInt(32.W)
  val eg = SInt(32.W)
  val eb = SInt(32.W)

  val dquorj = SInt(32.W)
  val dquogj = SInt(32.W)
  val dquobj = SInt(32.W)

  val dremrj = SInt(32.W)
  val dremgj = SInt(32.W)
  val drembj = SInt(32.W)

  val dquori = SInt(32.W)
  val dquogi = SInt(32.W)
  val dquobi = SInt(32.W)

  val dremri = SInt(32.W)
  val dremgi = SInt(32.W)
  val drembi = SInt(32.W)

  val dquorc = SInt(32.W)
  val dquogc = SInt(32.W)
  val dquobc = SInt(32.W)

  val dremrc = SInt(32.W)
  val dremgc = SInt(32.W)
  val drembc = SInt(32.W)

  val dquorr = SInt(32.W)
  val dquogr = SInt(32.W)
  val dquobr = SInt(32.W)

  val dremrr = SInt(32.W)
  val dremgr = SInt(32.W)
  val drembr = SInt(32.W)
}

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

    val dx0 = x1 - x0
    val dx1 = x2 - x1
    val dx2 = x0 - x2
    diffInfos(i).dx0 := dx0.S
    diffInfos(i).dx1 := dx1.S
    diffInfos(i).dx2 := dx2.S

    val dy0 = y0 - y1
    val dy1 = y1 - y2
    val dy2 = y2 - y0
    diffInfos(i).dy0 := dy0.S
    diffInfos(i).dy1 := dy1.S
    diffInfos(i).dy2 := dy2.S

    val e0 = dx0 * y0 + dy0 * x0
    val e1 = dx1 * y1 + dy1 * x1
    val e2 = dx2 * y2 + dy2 * x2
    diffInfos(i).e0 := e0.S
    diffInfos(i).e1 := e1.S
    diffInfos(i).e2 := e2.S

    val a = dy0 * dx2 - dx0 * dy2
    diffInfos(i).a := math.abs(a).S

    val r = if (a == 0) 0 else e1 * 255 / a
    val g = if (a == 0) 0 else e2 * 255 / a
    val b = if (a == 0) 0 else e0 * 255 / a
    diffInfos(i).r := r.S
    diffInfos(i).g := g.S
    diffInfos(i).b := b.S

    val er = if (a == 0) 0 else if (a > 0) e1 * 255 % a else -e1 * 255 % a
    val eg = if (a == 0) 0 else if (a > 0) e2 * 255 % a else -e2 * 255 % a
    val eb = if (a == 0) 0 else if (a > 0) e0 * 255 % a else -e0 * 255 % a
    diffInfos(i).er := er.S
    diffInfos(i).eg := eg.S
    diffInfos(i).eb := eb.S

    val dividendrj = -dy1 * 255
    val dividendgj = -dy2 * 255
    val dividendbj = -dy0 * 255

    diffInfos(i).dquorj := (if (a == 0) 0 else dividendrj / a).S
    diffInfos(i).dquogj := (if (a == 0) 0 else dividendgj / a).S
    diffInfos(i).dquobj := (if (a == 0) 0 else dividendbj / a).S

    diffInfos(i).dremrj := (if (a == 0) 0 else if (a > 0) dividendrj % a else -dividendrj % a).S
    diffInfos(i).dremgj := (if (a == 0) 0 else if (a > 0) dividendgj % a else -dividendgj % a).S
    diffInfos(i).drembj := (if (a == 0) 0 else if (a > 0) dividendbj % a else -dividendbj % a).S

    val dividendri = (-dx1 + dy1 * (Tile.size - 1)) * 255
    val dividendgi = (-dx2 + dy2 * (Tile.size - 1)) * 255
    val dividendbi = (-dx0 + dy0 * (Tile.size - 1)) * 255

    diffInfos(i).dquori := (if (a == 0) 0 else dividendri / a).S
    diffInfos(i).dquogi := (if (a == 0) 0 else dividendgi / a).S
    diffInfos(i).dquobi := (if (a == 0) 0 else dividendbi / a).S

    diffInfos(i).dremri := (if (a == 0) 0 else if (a > 0) dividendri % a else -dividendri % a).S
    diffInfos(i).dremgi := (if (a == 0) 0 else if (a > 0) dividendgi % a else -dividendgi % a).S
    diffInfos(i).drembi := (if (a == 0) 0 else if (a > 0) dividendbi % a else -dividendbi % a).S

    val dividendrc = (-dy1 + dx1) * Tile.size * 255
    val dividendgc = (-dy2 + dx2) * Tile.size * 255
    val dividendbc = (-dy0 + dx0) * Tile.size * 255

    diffInfos(i).dquorc := (if (a == 0) 0 else dividendrc / a).S
    diffInfos(i).dquogc := (if (a == 0) 0 else dividendgc / a).S
    diffInfos(i).dquobc := (if (a == 0) 0 else dividendbc / a).S

    diffInfos(i).dremrc := (if (a == 0) 0 else if (a > 0) dividendrc % a else -dividendrc % a).S
    diffInfos(i).dremgc := (if (a == 0) 0 else if (a > 0) dividendgc % a else -dividendgc % a).S
    diffInfos(i).drembc := (if (a == 0) 0 else if (a > 0) dividendbc % a else -dividendbc % a).S

    val dividendrr = dy1 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendgr = dy2 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendbr = dy0 * (Tile.nrCols - 1) * Tile.size * 255

    diffInfos(i).dquorr := (if (a == 0) 0 else dividendrr / a).S
    diffInfos(i).dquogr := (if (a == 0) 0 else dividendgr / a).S
    diffInfos(i).dquobr := (if (a == 0) 0 else dividendbr / a).S

    diffInfos(i).dremrr := (if (a == 0) 0 else if (a > 0) dividendrr % a else -dividendrr % a).S
    diffInfos(i).dremgr := (if (a == 0) 0 else if (a > 0) dividendgr % a else -dividendgr % a).S
    diffInfos(i).drembr := (if (a == 0) 0 else if (a > 0) dividendbr % a else -dividendbr % a).S
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
  val ie0 = RegInit(diffInfos(0).e0)
  val ie1 = RegInit(diffInfos(0).e1)
  val ie2 = RegInit(diffInfos(0).e2)
  val ce0 = RegInit(diffInfos(0).e0)
  val ce1 = RegInit(diffInfos(0).e1)
  val ce2 = RegInit(diffInfos(0).e2)
  val re0 = RegInit(diffInfos(0).e0)
  val re1 = RegInit(diffInfos(0).e1)
  val re2 = RegInit(diffInfos(0).e2)

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
    ie0 := diffInfos(angle).e0
    ie1 := diffInfos(angle).e1
    ie2 := diffInfos(angle).e2
    ce0 := diffInfos(angle).e0
    ce1 := diffInfos(angle).e1
    ce2 := diffInfos(angle).e2
    re0 := diffInfos(angle).e0
    re1 := diffInfos(angle).e1
    re2 := diffInfos(angle).e2

    r := diffInfos(angle).r
    g := diffInfos(angle).g
    b := diffInfos(angle).b

    er := diffInfos(angle).r
    eg := diffInfos(angle).g
    eb := diffInfos(angle).b

    row := 0.U
  }

  val tileWriter = Module(new TileWriter)
  val valid = RegInit(false.B)
  tileWriter.io.inReq.valid := valid
  when (valid && tileWriter.io.inReq.ready) {
    valid := false.B

    val ne0 = ce0 - diffInfo.dy0 * Tile.size.S
    e0  := ne0
    ie0 := ne0
    ce0 := ne0

    val ne1 = ce1 - diffInfo.dy1 * Tile.size.S
    e1  := ne1
    ie1 := ne1
    ce1 := ne1

    val ne2 = ce2 - diffInfo.dy2 * Tile.size.S
    e2  := ne2
    ie2 := ne2
    ce2 := ne2

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
      val ne0 = re0 - diffInfo.dx0 * Tile.size.S
      e0  := ne0
      ie0 := ne0
      ce0 := ne0
      re0 := ne0

      val ne1 = re1 - diffInfo.dx1 * Tile.size.S
      e1  := ne1
      ie1 := ne1
      ce1 := ne1
      re1 := ne1

      val ne2 = re2 - diffInfo.dx2 * Tile.size.S
      e2  := ne2
      ie2 := ne2
      ce2 := ne2
      re2 := ne2

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

    e0 := e0 - diffInfo.dy0
    e1 := e1 - diffInfo.dy1
    e2 := e2 - diffInfo.dy2

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

      val ne0 = ie0 - diffInfo.dx0
      e0  := ne0
      ie0 := ne0
      val ne1 = ie1 - diffInfo.dx1
      e1  := ne1
      ie1 := ne1
      val ne2 = ie2 - diffInfo.dx2
      e2  := ne2
      ie2 := ne2

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
