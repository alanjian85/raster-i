// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Render extends Module {
    val io = IO(new Bundle {
      val axi = new WrAxi(28, 128)
    })

    val xReg = RegInit(0.U(log2Up(Screen.width / 4).W))
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

    val rastAxReg = RegNext(vertShader.io.ax)
    val rastAyReg = RegNext(vertShader.io.ay)
    val rastBxReg = RegNext(vertShader.io.bx)
    val rastByReg = RegNext(vertShader.io.by)
    val rastBzReg = RegNext(vertShader.io.bz)
    val rastCxReg = RegNext(vertShader.io.cx)
    val rastCyReg = RegNext(vertShader.io.cy)
    val rastCzReg = RegNext(vertShader.io.cz)
    val rastPxReg = RegNext(RegNext(RegNext(xReg)))
    val rastPyReg = RegNext(RegNext(RegNext(yReg)))
 
    val pix = Wire(Vec(4, UInt(32.W)))
    val vis = Wire(Vec(4, Bool()))
    for (i <- 0 until 4) {
      val rasterizer = Module(new Rasterizer)
      rasterizer.io.ax := rastAxReg
      rasterizer.io.ay := rastAyReg
      rasterizer.io.bx := rastBxReg
      rasterizer.io.by := rastByReg
      rasterizer.io.bz := rastBzReg
      rasterizer.io.cx := rastCxReg
      rasterizer.io.cy := rastCyReg
      rasterizer.io.cz := rastCzReg
      rasterizer.io.px := (rastPxReg << 2.U) | i.U
      rasterizer.io.py := rastPyReg

      val fragShader = Module(new FragShader)
      fragShader.io.inVis := RegNext(rasterizer.io.visible)
      fragShader.io.u     := RegNext(rasterizer.io.u)
      fragShader.io.v     := RegNext(rasterizer.io.v)
      fragShader.io.w     := RegNext(rasterizer.io.w)
      fragShader.io.a     := RegNext(rasterizer.io.a)
      pix(i) := fragShader.io.pix
      vis(i) := fragShader.io.outVis
    }

    val pxReg = Reg(Vec(12, UInt(log2Up(Screen.width / 4).W)))
    val pyReg = Reg(Vec(12, UInt(log2Up(Screen.height).W)))
    pxReg(0) := rastPxReg
    pyReg(0) := rastPyReg
    for (i <- 1 until 12) {
      pxReg(i) := pxReg(i - 1)
      pyReg(i) := pyReg(i - 1)
    }
    val ditherer = Module(new Ditherer)
    ditherer.io.py := pyReg(11)
    ditherer.io.inPix := RegNext(pix)

    val fbWriter = Module(new FbWriter)
    fbWriter.io.req.bits.pix := RegNext(ditherer.io.outPix)
    fbWriter.io.req.bits.vis := RegNext(RegNext(vis))
    val reqXReg = RegNext(pxReg(11))
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
    val flushCntReg = RegInit(0.U(unsignedBitLength(15).W))
    when (flushReg) {
      fbWriter.io.req.valid := false.B
      flushCntReg := flushCntReg + 1.U
      xReg := xReg + 1.U
      when (xReg === ((Screen.width / 4) - 1).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          yReg := 0.U
          frameAngleReg := angleReg
        }
      }
      when (flushCntReg === 15.U) {
        flushReg := false.B
      }
    } .otherwise {
      fbWriter.io.req.valid := true.B
      when (fbWriter.io.req.ready) {
        xReg := xReg + 1.U
        when (xReg === ((Screen.width / 4) - 1).U) {
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
