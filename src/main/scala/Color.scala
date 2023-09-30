// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class RGB(val rWidth: Int, val gWidth: Int, val bWidth: Int) extends Bundle {
  val r = UInt(rWidth.W)
  val g = UInt(gWidth.W)
  val b = UInt(bWidth.W)
}

class RGBFactory(val rWidth: Int, val gWidth: Int, val bWidth: Int) {
  def apply() = new RGB(rWidth, gWidth, bWidth)

  def encode(pix: RGB) = {
    pix.b ## pix.g ## pix.r
  }

  def decode(pix: UInt) = {
    val result = Wire(new RGB(rWidth, gWidth, bWidth))
    result.r := pix(rWidth - 1, 0)
    result.g := pix(rWidth + gWidth - 1, rWidth)
    result.b := pix(rWidth + gWidth + bWidth - 1, rWidth + gWidth)
    result
  }
}

object RGB444 extends RGBFactory(4, 4, 4)
object RGB888 extends RGBFactory(8, 8, 8)
