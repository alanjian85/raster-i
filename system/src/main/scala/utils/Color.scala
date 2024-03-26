// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import scala.math._

class RGB(val rWidth: Int, val gWidth: Int, val bWidth: Int) extends Bundle {
  val succinctWidth = rWidth + gWidth + bWidth
  val alignedWidth  = pow(2, log2Up(succinctWidth)).toInt
  val nrBytes       = alignedWidth / 8

  val r = UInt(rWidth.W)
  val g = UInt(gWidth.W)
  val b = UInt(bWidth.W)

  def encode() = b ## g ## r

  def encodeAligned() = 0.U((alignedWidth - succinctWidth).W) ## b ## g ## r

  def map(f: (UInt) => UInt) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := f(r)
    res.g := f(g)
    res.b := f(b)
    res
  }
}

class RGBFactory(val rWidth: Int, val gWidth: Int, val bWidth: Int) {
  val succinctWidth = rWidth + gWidth + bWidth
  val alignedWidth  = pow(2, log2Up(succinctWidth)).toInt
  val nrBytes       = alignedWidth / 8

  def apply() = new RGB(rWidth, gWidth, bWidth)

  def apply(x: Int) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := x.U(rWidth.W)
    res.g := x.U(gWidth.W)
    res.b := x.U(bWidth.W)
    res
  }

  def apply(x: UInt) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := x
    res.g := x
    res.b := x
    res
  }

  def apply(r: UInt, g: UInt, b: UInt) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := r
    res.g := g
    res.b := b
    res
  }

  def decode(pix: UInt) = {
    val res = Wire(new RGB(rWidth, gWidth, bWidth))
    res.r := pix(rWidth - 1, 0)
    res.g := pix(rWidth + gWidth - 1, rWidth)
    res.b := pix(rWidth + gWidth + bWidth - 1, rWidth + gWidth)
    res
  }
}
