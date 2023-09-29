// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Ditherer extends Module {
    val io = IO(new Bundle {
        val row = Input(UInt(2.W))
        val inPix = Input(Vec(4, UInt(24.W)))
        val outPix = Output(Vec(4, UInt(12.W)))
    })

    def clampAdd(x: UInt, y: UInt) = {
        val sum = x +& y
        val result = WireDefault(sum(7, 4))
        when (sum(8)) {
            result := "hf".U
        }
        result
    }

    def dither(pix: UInt, elem: UInt) = {
      clampAdd(pix(23, 16), elem) ##
      clampAdd(pix(15,  8), elem) ##
      clampAdd(pix( 7,  0), elem)
    }

    val kernel = VecInit(
        VecInit( 0.U,  8.U,  2.U, 10.U),
        VecInit(12.U,  4.U, 14.U,  6.U),
        VecInit( 3.U, 11.U,  1.U,  9.U),
        VecInit(15.U,  7.U, 13.U,  5.U)
    )

    io.outPix := VecInit(dither(io.inPix(0), kernel(io.row)(0)),
                         dither(io.inPix(1), kernel(io.row)(1)),
                         dither(io.inPix(2), kernel(io.row)(2)),
                         dither(io.inPix(3), kernel(io.row)(3)))
}
