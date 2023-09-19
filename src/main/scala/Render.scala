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
    val frameAngleReg = RegInit(0.U(log2Up(360).W))

    val cosRom = Module(new CosRom)
    val bzRom  = Module(new BzRom)
    val czRom  = Module(new CzRom)
    cosRom.io.addr := frameAngleReg
    bzRom.io.addr  := frameAngleReg
    czRom.io.addr  := frameAngleReg
    
    val vertShader = Module(new VertShader)
    vertShader.io.cos  := cosRom.io.data
    vertShader.io.inBz := bzRom.io.data
    vertShader.io.inCz := czRom.io.data

    val rasterizer = Module(new Rasterizer)
    rasterizer.io.ax := RegNext(vertShader.io.ax)
    rasterizer.io.ay := RegNext(vertShader.io.ay)
    rasterizer.io.bx := RegNext(vertShader.io.bx)
    rasterizer.io.by := RegNext(vertShader.io.by)
    rasterizer.io.bz := RegNext(vertShader.io.bz)
    rasterizer.io.cx := RegNext(vertShader.io.cx)
    rasterizer.io.cy := RegNext(vertShader.io.cy)
    rasterizer.io.cz := RegNext(vertShader.io.cz)
    rasterizer.io.px := RegNext(RegNext(RegNext(xReg)))
    rasterizer.io.py := RegNext(RegNext(RegNext(yReg)))

    val fragShader = Module(new FragShader)
    fragShader.io.visible := RegNext(rasterizer.io.visible)
    fragShader.io.u       := RegNext(rasterizer.io.u)
    fragShader.io.v       := RegNext(rasterizer.io.v)
    fragShader.io.w       := RegNext(rasterizer.io.w)
    fragShader.io.a       := RegNext(rasterizer.io.a)

    val ditherer = Module(new Ditherer)
    ditherer.io.px := RegNext(RegNext(RegNext(rasterizer.io.px)))
    ditherer.io.py := RegNext(RegNext(RegNext(rasterizer.io.py)))
    ditherer.io.inPix := RegNext(fragShader.io.pix)

    val fbWriter = Module(new FbWriter)
    fbWriter.io.req.bits := RegNext(ditherer.io.outPix)
    val reqXReg = RegNext(ditherer.io.px)
    val reqYReg = RegNext(ditherer.io.py)
    io.axi <> fbWriter.io.axi

    val cntReg = RegInit(0.U(unsignedBitLength(1388888).W))
    val angleReg = RegInit(0.U(log2Up(360).W))
    cntReg := cntReg + 1.U
    when (cntReg === 1388888.U) {
      cntReg := 0.U
      angleReg := angleReg + 1.U
      when (angleReg === 359.U) {
        angleReg := 0.U
      }
    }

    val flushReg    = RegInit(true.B)
    val flushCntReg = RegInit(0.U(unsignedBitLength(6).W))
    when (flushReg) {
      fbWriter.io.req.valid := false.B
      flushCntReg := flushCntReg + 1.U
      xReg := xReg + 1.U
      when (xReg === (Screen.width - 1).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          yReg := 0.U
          frameAngleReg := angleReg
        }
      }
      when (flushCntReg === 6.U) {
        flushReg := false.B
      }
    } .otherwise {
      fbWriter.io.req.valid := true.B
      when (fbWriter.io.req.ready) {
        xReg := xReg + 1.U
        when (xReg === (Screen.width - 1).U) {
          xReg := 0.U
          yReg := yReg + 1.U
          when (yReg === (Screen.height - 1).U) {
            yReg := 0.U
            frameAngleReg := angleReg
          }
        }
      } .otherwise {
        flushReg := true.B
        flushCntReg := 0.U
        xReg := reqXReg
        yReg := reqYReg
      }
    }
}
