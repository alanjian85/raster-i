// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Ditherer extends Module {
    val io = IO(new Bundle {
        val px = Input(UInt(log2Up(Screen.width).W))
        val py = Input(UInt(log2Up(Screen.height).W))
        val inPix = Input(UInt(32.W))
        val outPix = Output(UInt(32.W))
    })

    def clampAdd(x: UInt, y: UInt) = {
        val sum = x +& y
        val result = WireDefault(sum(7, 0))
        when (sum(8)) {
            result := 255.U
        }
        result
    }

    val kernel = VecInit(
        VecInit( 0.U,  8.U,  2.U, 10.U),
        VecInit(12.U,  4.U, 14.U,  6.U),
        VecInit( 3.U, 11.U,  1.U,  9.U),
        VecInit(15.U,  7.U, 13.U,  5.U)
    )

    val col = io.px(1, 0)
    val row = io.py(1, 0)
    io.outPix := clampAdd(io.inPix(31, 24), kernel(row)(col)) ##
                 clampAdd(io.inPix(23, 16), kernel(row)(col)) ##
                 clampAdd(io.inPix(15,  8), kernel(row)(col)) ##
                 clampAdd(io.inPix( 7,  0), kernel(row)(col))
}
