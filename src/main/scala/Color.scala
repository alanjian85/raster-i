// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class RGB(width: Int) extends Bundle {
  val r = UInt(width.W)
  val g = UInt(width.W)
  val b = UInt(width.W)
}

class RGB4 extends RGB(4)

object RGB4 {
  def apply() = {
    new RGB4
  }
}

object RGB4Init {
  def apply(x: UInt = 0.U) = {
    val result = Wire(RGB4())
    result.r := x
    result.g := x
    result.b := x
    result
  }

  def apply(r: UInt, g: UInt, b: UInt) = {
    val result = Wire(RGB4())
    result.r := r
    result.g := g
    result.b := b
    result
  }
}
