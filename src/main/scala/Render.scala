// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class RenderReq extends Bundle {
  val x = UInt(log2Up(Screen.width).W)
  val y = UInt(log2Up(Screen.height).W)
}

class Render extends Module {
    val io = IO(new Bundle {
      val axi = new WrAxi(28, 128)
    })

    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))

    val pixels = Wire(Vec(4, UInt(32.W)))
    for (i <- 0 until 4) {
      val shader = Module(new Shader)
      shader.io.x := xReg | i.U
      shader.io.y := yReg
      pixels(i) := shader.io.pix
    }

    val dither = Module(new Dither)
    dither.io.inReq.bits.x := xReg
    dither.io.inReq.bits.y := yReg
    dither.io.inReq.bits.pix := pixels
    dither.io.inReq.valid := true.B

    val fbWriter = Module(new FbWriter)
    fbWriter.io.req <> dither.io.outReq

    io.axi <> fbWriter.io.axi

    when (dither.io.inReq.valid && dither.io.inReq.ready) {
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