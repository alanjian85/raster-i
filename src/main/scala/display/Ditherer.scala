// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Ditherer extends Module {
  val io = IO(new Bundle {
    val in  = Input(Vec(4, FbRGB()))
    val row = Input(UInt(2.W))
    val out = Output(Vec(4, VgaRGB()))
  })

  object BayerMat4 extends UMat4Factory(4)
  val bayer = BayerMat4(
     0,  8,  2, 10,
    12,  4, 14,  6,
     3, 11,  1,  9,
    15,  7, 13,  5
  )

  def dither(x: UInt, threshold: UInt) = {
    val sum = x +& threshold
    val res = WireDefault(sum(7, 4))
    when (sum(8)) {
        res := "hf".U
    }
    res
  }

  def ditherRGB(pix: RGB, threshold: UInt) = {
    val res = Wire(VgaRGB())
    res.r := dither(pix.r, threshold)
    res.g := dither(pix.g, threshold)
    res.b := dither(pix.b, threshold)
    res
  }

  io.out(0) := ditherRGB(io.in(0), bayer(io.row)(0))
  io.out(1) := ditherRGB(io.in(1), bayer(io.row)(1))
  io.out(2) := ditherRGB(io.in(2), bayer(io.row)(2))
  io.out(3) := ditherRGB(io.in(3), bayer(io.row)(3))
}
