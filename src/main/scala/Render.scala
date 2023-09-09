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

    val pixVec = Wire(Vec(4, UInt(32.W)))
    for (i <- 0 until 4) {
      val shader = Module(new Shader)
      shader.io.x := xReg | i.U
      shader.io.y := yReg
      pixVec(i) := shader.io.pix
    }

    val ditherer = Module(new Ditherer)
    ditherer.io.y := yReg
    ditherer.io.inPixVec := RegNext(pixVec)

    val fbWriter = Module(new FbWriter)
    fbWriter.io.req.valid := false.B
    fbWriter.io.req.bits.x := xReg
    fbWriter.io.req.bits.y := yReg
    fbWriter.io.req.bits.pixVec := RegNext(ditherer.io.outPixVec)
    io.axi <> fbWriter.io.axi

    object State extends ChiselEnum {
      val shading, dithering, writing = Value
    }
    import State._

    val stateReg = RegInit(shading)
    switch (stateReg) {
      is(shading) {
        stateReg := dithering
      }
      is(dithering) {
        stateReg := writing        
      }
      is(writing) {
        fbWriter.io.req.valid := true.B
        when (fbWriter.io.req.ready) {
          stateReg := State.shading
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
    }
}