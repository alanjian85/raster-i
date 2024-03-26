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
    val res = Wire(new UVec2(xWidth, yWidth))
    res.x := x.U
    res.y := y.U
    res
  }
}
