// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Render extends Module {
    val io = IO(new Bundle {
      val axi = new WrAxi(28, 128)
    })

    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    
    val vertShader = Module(new VertShader)

    val pixVec = Wire(Vec(4, UInt(32.W)))
    for (i <- 0 until 4) {
      val rasterizer = Module(new Rasterizer)
      rasterizer.io.ax := RegNext(vertShader.io.ax)
      rasterizer.io.ay := RegNext(vertShader.io.ay)
      rasterizer.io.bx := RegNext(vertShader.io.bx)
      rasterizer.io.by := RegNext(vertShader.io.by)
      rasterizer.io.cx := RegNext(vertShader.io.cx)
      rasterizer.io.cy := RegNext(vertShader.io.cy)
      rasterizer.io.px := xReg | i.U
      rasterizer.io.py := yReg

      val fragShader = Module(new FragShader)
      fragShader.io.visible := RegNext(rasterizer.io.visible)
      fragShader.io.u       := RegNext(rasterizer.io.u)
      fragShader.io.v       := RegNext(rasterizer.io.v)
      fragShader.io.w       := RegNext(rasterizer.io.w)
      fragShader.io.a       := RegNext(rasterizer.io.a)

      pixVec(i) := fragShader.io.pix
    }

    val ditherer = Module(new Ditherer)
    ditherer.io.py := yReg
    ditherer.io.inPixVec := RegNext(pixVec)

    val fbWriter = Module(new FbWriter)
    fbWriter.io.req.valid := false.B
    fbWriter.io.req.bits.x := xReg
    fbWriter.io.req.bits.y := yReg
    fbWriter.io.req.bits.pixVec := RegNext(ditherer.io.outPixVec)
    io.axi <> fbWriter.io.axi

    object State extends ChiselEnum {
      val vertShading, rasterizing, fragShading, dithering, writing = Value
    }
    import State._

    val stateReg = RegInit(vertShading)
    switch (stateReg) {
      is(vertShading) {
        stateReg := rasterizing
      }
      is(rasterizing) {
        stateReg := fragShading
      }
      is(fragShading) {
        stateReg := dithering
      }
      is(dithering) {
        stateReg := writing        
      }
      is(writing) {
        fbWriter.io.req.valid := true.B
        when (fbWriter.io.req.ready) {
          stateReg := State.vertShading
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