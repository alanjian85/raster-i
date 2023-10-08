// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import scala.math._

class RGB(val rWidth: Int, val gWidth: Int, val bWidth: Int) extends Bundle {
  val r = UInt(rWidth.W)
  val g = UInt(gWidth.W)
  val b = UInt(bWidth.W)
}

class RGBFactory(val rWidth: Int, val gWidth: Int, val bWidth: Int) {
  val width        = rWidth + gWidth + bWidth
  val alignedWidth = pow(2, log2Up(width)).toInt
  val nrBytes      = alignedWidth / 8

  def apply() = new RGB(rWidth, gWidth, bWidth)

  def encode(pix: RGB) = pix.b ## pix.g ## pix.r

  def encodeAligned(pix: RGB) = 0.U((alignedWidth - width).W) ## pix.b ## pix.g ## pix.r

  def decode(pix: UInt) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := pix(rWidth - 1, 0)
    res.g := pix(rWidth + gWidth - 1, rWidth)
    res.b := pix(rWidth + gWidth + bWidth - 1, rWidth + gWidth)
    res
  }
}
