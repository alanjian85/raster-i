// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class UVec2 extends Bundle {
  val x = UInt()
  val y = UInt()

  def asSVec2() = {
    val result = Wire(SVec2())
    result.x := x.asSInt
    result.y := y.asSInt
    result
  }
  
  def +(that: UVec2) = {
    val result = Wire(UVec2())
    result.x := this.x + that.x
    result.x := this.y + that.y
    result
  }

  def -(that: UVec2) = {
    val result = Wire(UVec2())
    result.x := this.x - that.x
    result.y := this.y - that.y
    result
  }
}

object UVec2 {
  def apply() = {
    new UVec2
  }
}

object UVec2Init {
  def apply(x: UInt = 0.U) = {
    val result = Wire(UVec2())
    result.x := x
    result.y := x
    result
  }

  def apply(x: UInt, y: UInt) = {
    val result = Wire(UVec2())
    result.x := x
    result.y := y
    result
  }
}

class SVec2 extends Bundle {
  val x = SInt()
  val y = SInt()

  def asUVec2() = {
    val result = Wire(UVec2())
    result.x := x.asUInt
    result.y := y.asUInt
    result
  }

  def +(that: SVec2) = {
    val result = Wire(SVec2())
    result.x := this.x + that.x
    result.y := this.y + that.y
    result
  }

  def -(that: SVec2) = {
    val result = Wire(SVec2())
    result.x := this.x - that.x
    result.y := this.y - that.y
    result
  }
}

object SVec2 {
  def apply() = {
    new SVec2
  }
}

object SVec2Init {
  def apply(x: SInt = 0.S) = {
    val result = Wire(SVec2())
    result.x := x
    result.y := x
    result
  }

  def apply(x: SInt, y: SInt) = {
    val result = Wire(SVec2())
    result.x := x
    result.y := y
    result
  }
}
