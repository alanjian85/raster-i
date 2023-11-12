import chisel3._

object DiffInfo {
  def build(p0: (Int, Int), p1: (Int, Int), p2: (Int, Int)) =  {
    val diffInfo = Wire(new DiffInfo)

    val dx0 = p1._1 - p0._1
    val dx1 = p2._1 - p1._1
    val dx2 = p0._1 - p2._1
    diffInfo.dx0 := dx0.S
    diffInfo.dx1 := dx1.S
    diffInfo.dx2 := dx2.S

    val dy0 = p0._2 - p1._2
    val dy1 = p1._2 - p2._2
    val dy2 = p2._2 - p0._2
    diffInfo.dy0 := dy0.S
    diffInfo.dy1 := dy1.S
    diffInfo.dy2 := dy2.S

    val e0 = dx0 * p0._2 + dy0 * p0._1
    val e1 = dx1 * p1._2 + dy1 * p1._1
    val e2 = dx2 * p2._2 + dy2 * p2._1
    diffInfo.e0 := e0.S
    diffInfo.e1 := e1.S
    diffInfo.e2 := e2.S

    val a = dy0 * dx2 - dx0 * dy2
    diffInfo.a := math.abs(a).S

    val r = if (a == 0) 0 else e1 * 255 / a
    val g = if (a == 0) 0 else e2 * 255 / a
    val b = if (a == 0) 0 else e0 * 255 / a
    diffInfo.r := r.S
    diffInfo.g := g.S
    diffInfo.b := b.S

    val er = if (a == 0) 0 else if (a > 0) e1 * 255 % a else -e1 * 255 % a
    val eg = if (a == 0) 0 else if (a > 0) e2 * 255 % a else -e2 * 255 % a
    val eb = if (a == 0) 0 else if (a > 0) e0 * 255 % a else -e0 * 255 % a
    diffInfo.er := er.S
    diffInfo.eg := eg.S
    diffInfo.eb := eb.S

    val dividendrj = -dy1 * 255
    val dividendgj = -dy2 * 255
    val dividendbj = -dy0 * 255

    diffInfo.dquorj := (if (a == 0) 0 else dividendrj / a).S
    diffInfo.dquogj := (if (a == 0) 0 else dividendgj / a).S
    diffInfo.dquobj := (if (a == 0) 0 else dividendbj / a).S

    diffInfo.dremrj := (if (a == 0) 0 else if (a > 0) dividendrj % a else -dividendrj % a).S
    diffInfo.dremgj := (if (a == 0) 0 else if (a > 0) dividendgj % a else -dividendgj % a).S
    diffInfo.drembj := (if (a == 0) 0 else if (a > 0) dividendbj % a else -dividendbj % a).S

    val dividendri = (-dx1 + dy1 * (Tile.size - 1)) * 255
    val dividendgi = (-dx2 + dy2 * (Tile.size - 1)) * 255
    val dividendbi = (-dx0 + dy0 * (Tile.size - 1)) * 255

    diffInfo.dquori := (if (a == 0) 0 else dividendri / a).S
    diffInfo.dquogi := (if (a == 0) 0 else dividendgi / a).S
    diffInfo.dquobi := (if (a == 0) 0 else dividendbi / a).S

    diffInfo.dremri := (if (a == 0) 0 else if (a > 0) dividendri % a else -dividendri % a).S
    diffInfo.dremgi := (if (a == 0) 0 else if (a > 0) dividendgi % a else -dividendgi % a).S
    diffInfo.drembi := (if (a == 0) 0 else if (a > 0) dividendbi % a else -dividendbi % a).S

    val dividendrc = (-dy1 + dx1) * Tile.size * 255
    val dividendgc = (-dy2 + dx2) * Tile.size * 255
    val dividendbc = (-dy0 + dx0) * Tile.size * 255

    diffInfo.dquorc := (if (a == 0) 0 else dividendrc / a).S
    diffInfo.dquogc := (if (a == 0) 0 else dividendgc / a).S
    diffInfo.dquobc := (if (a == 0) 0 else dividendbc / a).S

    diffInfo.dremrc := (if (a == 0) 0 else if (a > 0) dividendrc % a else -dividendrc % a).S
    diffInfo.dremgc := (if (a == 0) 0 else if (a > 0) dividendgc % a else -dividendgc % a).S
    diffInfo.drembc := (if (a == 0) 0 else if (a > 0) dividendbc % a else -dividendbc % a).S

    val dividendrr = dy1 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendgr = dy2 * (Tile.nrCols - 1) * Tile.size * 255
    val dividendbr = dy0 * (Tile.nrCols - 1) * Tile.size * 255

    diffInfo.dquorr := (if (a == 0) 0 else dividendrr / a).S
    diffInfo.dquogr := (if (a == 0) 0 else dividendgr / a).S
    diffInfo.dquobr := (if (a == 0) 0 else dividendbr / a).S

    diffInfo.dremrr := (if (a == 0) 0 else if (a > 0) dividendrr % a else -dividendrr % a).S
    diffInfo.dremgr := (if (a == 0) 0 else if (a > 0) dividendgr % a else -dividendgr % a).S
    diffInfo.drembr := (if (a == 0) 0 else if (a > 0) dividendbr % a else -dividendbr % a).S

    diffInfo
  }
}

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
