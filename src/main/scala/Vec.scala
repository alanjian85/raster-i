import chisel3._

class UVec2(width: Int) extends Bundle {
  val x = UInt(width.W)
  val y = UInt(width.W)

  def asSVec2() = {
    val result = Wire(new SVec2(x.getWidth + 1))
    result.x := x.asSInt
    result.y := y.asSInt
    result
  }
  
  def +(that: UVec2) = {
    assert(this.x.getWidth == that.x.getWidth)
    val result = Wire(new UVec2(x.getWidth))
    result.x := this.x + that.x
    result.x := this.y + that.y
    result
  }

  def -(that: UVec2) = {
    assert(this.x.getWidth == that.x.getWidth)
    val result = Wire(new UVec2(x.getWidth))
    result.x := this.x - that.x
    result.y := this.y - that.y
    result
  }
}

class SVec2(width: Int) extends Bundle {
  val x = SInt(width.W)
  val y = SInt(width.W)

  def asUVec2() = {
    val result = Wire(new UVec2(x.getWidth - 1))
    result.x := x.asUInt
    result.y := y.asUInt
    result
  }

  def +(that: SVec2) = {
    assert(this.x.getWidth == that.x.getWidth)
    val result = Wire(new SVec2(x.getWidth))
    result.x := this.x + that.x
    result.y := this.y + that.y
    result
  }

  def -(that: SVec2) = {
    assert(this.x.getWidth == that.x.getWidth)
    val result = Wire(new SVec2(x.getWidth))
    result.x := this.x - that.x
    result.y := this.y - that.y
    result
  }
}
