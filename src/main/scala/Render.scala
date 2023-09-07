// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Render extends Module {
    val io = IO(new Bundle {
      val axi = new WrAxi(28, 128)
    })

    val fbWriter = Module(new FbWriter)
    io.axi <> fbWriter.io.axi

    val dither = Module(new Dither)
    fbWriter.io.req <> dither.io.outReq

    val validReg = RegInit(true.B)
    validReg := true.B
    dither.io.inReq.valid := validReg

    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    dither.io.inReq.bits.x := xReg
    dither.io.inReq.bits.y := yReg

    val imgRom = Module(new ImgRom)
    imgRom.io.addr := (yReg >> 2.U) * (Screen.width >> 4).U + (xReg >> 4.U)
    dither.io.inReq.bits.pix := VecInit(Seq.fill(4)(0.U(32.W)))
    switch (xReg(3, 2)) {
      is(0.U) {
        dither.io.inReq.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(31, 0)))
      }
      is(1.U) {
        dither.io.inReq.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(63, 32)))
      }
      is(2.U) {
        dither.io.inReq.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(95, 64)))
      }
      is(3.U) {
        dither.io.inReq.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(127, 96)))
      }
    }

    when (dither.io.inReq.valid && dither.io.inReq.ready) {
      validReg := false.B
      xReg := xReg + 4.U
      when (xReg === (Screen.width - 4).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          yReg := 0.U
        }
      }
    }
}