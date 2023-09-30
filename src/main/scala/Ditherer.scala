// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Ditherer extends Module {
  val io = IO(new Bundle {
    val row = Input(UInt(2.W))
    val in  = Input(Vec(4, RGB888()))
    val out = Output(Vec(4, RGB444()))
  })

  val bayer = VecInit(
    VecInit( 0.U,  8.U,  2.U, 10.U),
    VecInit(12.U,  4.U, 14.U,  6.U),
    VecInit( 3.U, 11.U,  1.U,  9.U),
    VecInit(15.U,  7.U, 13.U,  5.U)
  )

  def dither(x: UInt, threshold: UInt) = {
    val sum = x +& threshold
    val result = WireDefault(sum(7, 4))
    when (sum(8)) {
        result := "hf".U
    }
    result
  }

  def ditherRGB(pix: RGB, threshold: UInt) = {
    val result = Wire(RGB444())
    result.r := dither(pix.r, threshold)
    result.g := dither(pix.g, threshold)
    result.b := dither(pix.b, threshold)
    result
  }

  io.out := VecInit(
    ditherRGB(io.in(0), bayer(io.row)(0)),
    ditherRGB(io.in(1), bayer(io.row)(1)),
    ditherRGB(io.in(2), bayer(io.row)(2)),
    ditherRGB(io.in(3), bayer(io.row)(3))
  )
}
