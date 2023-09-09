// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Dither extends Module {
    val io = IO(new Bundle {
        val inReq = Flipped(Decoupled(new FbWrReq))
        val outReq = Decoupled(new FbWrReq)
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

    val pixReg = Reg(Vec(4, UInt(32.W)))
    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    io.outReq.bits.pix := pixReg
    io.outReq.bits.x   := xReg
    io.outReq.bits.y   := yReg
    val emptyReg = RegInit(true.B)
    io.inReq.ready := emptyReg
    when (io.inReq.valid && io.inReq.ready) {
        xReg := io.inReq.bits.x
        yReg := io.inReq.bits.y
        val row = io.inReq.bits.y & "b11".U
        pixReg(0) := vecScalarAdd(io.inReq.bits.pix(0), kernel(row)(0))
        pixReg(1) := vecScalarAdd(io.inReq.bits.pix(1), kernel(row)(1))
        pixReg(2) := vecScalarAdd(io.inReq.bits.pix(2), kernel(row)(2))
        pixReg(3) := vecScalarAdd(io.inReq.bits.pix(3), kernel(row)(3))
        emptyReg := false.B
    }
    io.outReq.valid := !emptyReg
    when (io.outReq.valid && io.outReq.ready) {
        emptyReg := true.B
    }
}