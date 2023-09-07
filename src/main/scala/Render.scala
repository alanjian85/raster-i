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

    val validReg = RegInit(true.B)
    validReg := true.B
    fbWriter.io.req.valid := validReg

    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    fbWriter.io.req.bits.x := xReg
    fbWriter.io.req.bits.y := yReg

    val imgRom = Module(new ImgRom)
    imgRom.io.addr := (yReg >> 2.U) * (Screen.width >> 4).U + (xReg >> 4.U)
    fbWriter.io.req.bits.pix := VecInit(Seq.fill(4)(0.U(32.W)))
    switch (xReg(3, 2)) {
      is(0.U) {
        fbWriter.io.req.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(31, 0)))
      }
      is(1.U) {
        fbWriter.io.req.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(63, 32)))
      }
      is(2.U) {
        fbWriter.io.req.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(95, 64)))
      }
      is(3.U) {
        fbWriter.io.req.bits.pix := VecInit(Seq.fill(4)(imgRom.io.data(127, 96)))
      }
    }

    when (fbWriter.io.req.valid && fbWriter.io.req.ready) {
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