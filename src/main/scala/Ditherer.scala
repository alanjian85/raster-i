// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Ditherer extends Module {
    val io = IO(new Bundle {
        val py = Input(UInt(log2Up(Screen.height).W))
        val inPixVec = Input(Vec(4, UInt(32.W)))
        val outPixVec = Output(Vec(4, UInt(32.W)))
    })

    def clampAdd(x: UInt, y: UInt) = {
        val sum = (0.U ## x) + y
        val result = WireDefault(sum(7, 0))
        when (sum(8)) {
            result := 255.U
        }
        result
    }

    def vecScalarAdd(x: UInt, y: UInt) = {
        clampAdd(x(31, 24),  y) ##
        clampAdd(x(23, 16),  y) ##
        clampAdd(x(15,  8),  y) ##
        clampAdd(x( 7,  0),  y)
    }

    val kernel = VecInit(
        VecInit( 0.U,  8.U,  2.U, 10.U),
        VecInit(12.U,  4.U, 14.U,  6.U),
        VecInit( 3.U, 11.U,  1.U,  9.U),
        VecInit(15.U,  7.U, 13.U,  5.U)
    )

    val row = io.py & "b11".U
    io.outPixVec(0) := vecScalarAdd(io.inPixVec(0), kernel(row)(0))
    io.outPixVec(1) := vecScalarAdd(io.inPixVec(1), kernel(row)(1))
    io.outPixVec(2) := vecScalarAdd(io.inPixVec(2), kernel(row)(2))
    io.outPixVec(3) := vecScalarAdd(io.inPixVec(3), kernel(row)(3))
}