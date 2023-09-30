// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class UVec2(val xWidth: Int, val yWidth: Int) extends Bundle {
  val x = UInt(xWidth.W)
  val y = UInt(yWidth.W)
}

class UVec2Factory(val xWidth: Int, val yWidth: Int) {
  def apply() = new UVec2(xWidth, yWidth)

  def apply(x: Int, y: Int) = {
    val result = Wire(new UVec2(xWidth, yWidth))
    result.x := x.U(xWidth.W)
    result.y := y.U(yWidth.W)
    result
  }

  def apply(x: UInt, y: UInt) = {
    val result = Wire(new UVec2(xWidth, yWidth))
    result.x := x
    result.y := y
    result
  }
}

class SVec2(xWidth: Int, yWidth: Int) extends Bundle {
  val x = SInt(xWidth.W)
  val y = SInt(yWidth.W)
}

class SVec2Factory(val xWidth: Int, val yWidth: Int) {
  def apply() = new SVec2(xWidth, yWidth)

  def apply(x: Int, y: Int) = {
    val result = Wire(new SVec2(xWidth, yWidth))
    result.x := x.S(xWidth.W)
    result.y := y.S(yWidth.W)
    result
  }

  def apply(x: UInt, y: UInt) = {
    val result = Wire(new SVec2(xWidth, yWidth))
    result.x := x
    result.y := y
    result
  }
}
