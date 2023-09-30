// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class RGB(val rWidth: Int, val gWidth: Int, val bWidth: Int) extends Bundle {
  val r = UInt(rWidth.W)
  val g = UInt(gWidth.W)
  val b = UInt(bWidth.W)
}

class RGBFactory(val rWidth: Int, val gWidth: Int, val bWidth: Int) {
  def apply() = new RGB(rWidth, gWidth, bWidth)

  def apply(r: Int, g: Int, b: Int) = {
    require(r >= 0)
    require(unsignedBitLength(r) <= rWidth)
    require(g >= 0)
    require(unsignedBitLength(g) <= gWidth)
    require(b >= 0)
    require(unsignedBitLength(b) <= bWidth)
    val result = Wire(new RGB(rWidth, gWidth, bWidth))
    result.r := r.U(rWidth.W)
    result.g := g.U(gWidth.W)
    result.b := b.U(bWidth.W)
    result
  }

  def apply(r: UInt, g: UInt, b: UInt) = {
    val result = Wire(new RGB(rWidth, gWidth, bWidth))
    result.r := r
    result.g := g
    result.b := b
    result
  }
}

object RGB444 extends RGBFactory(4, 4, 4)
object RGB888 extends RGBFactory(8, 8, 8)
