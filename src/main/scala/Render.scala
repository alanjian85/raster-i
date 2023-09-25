// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Render extends Module {
  val io = IO(new Bundle {
    val vblank = Input(Bool())
    val axi = new WrAxi(28, 128)
    val fbIdx = Output(UInt(1.W))
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

  val fbIdx = RegInit(1.U(1.W)) 
  io.fbIdx := fbIdx

  val fbWriter = Module(new FbWriter)
  fbWriter.io.fbIdx := fbIdx
  fbWriter.io.req.valid := false.B
  fbWriter.io.req.bits.pix := RegNext(ditherer.io.outPix)
  fbWriter.io.req.bits.vis := RegNext(RegNext(vis))
  val reqXReg = RegNext(pxReg(11))
  val reqYReg = RegNext(ditherer.io.py)
  val angleReg = Reg(Vec(16, UInt(9.W)))
  angleReg(0) := frameAngleReg
  for (i <- 1 until 16) {
    angleReg(i) := angleReg(i - 1)
  }
  val reqAngleReg = angleReg(15)
  io.axi <> fbWriter.io.axi

  val cntReg = RegInit(0.U(unsignedBitLength(138888).W))
  val currAngleReg = RegInit(0.U(log2Up(360).W))
  cntReg := cntReg + 1.U
  when (cntReg === 138888.U) {
    cntReg := 0.U
    currAngleReg := currAngleReg + 1.U
    when (currAngleReg === 359.U) {
      currAngleReg := 0.U
    }
  }

  object State extends ChiselEnum {
    val flush, run, done = Value
  }
  import State._

  val state = RegInit(flush)
  val pipeCnt = RegInit(0.U(unsignedBitLength(16).W))
  switch (state) {
    is(flush) {
      fbWriter.io.req.valid := false.B
      pipeCnt := pipeCnt + 1.U
      xReg := xReg + 1.U
      when (xReg === ((Screen.width / 4) - 1).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          yReg := 0.U
        }
      }
      when (pipeCnt === 15.U) {
        pipeCnt := 0.U
        state := run
      }
    }
    is(run) {
      fbWriter.io.req.valid := true.B
      when (fbWriter.io.req.ready) {
        xReg := xReg + 1.U
        when (xReg === ((Screen.width / 4) - 1).U) {
          xReg := 0.U
          yReg := yReg + 1.U
          when (yReg === (Screen.height - 1).U) {
            yReg := 0.U
            state := done
          }
        }
      } .otherwise {
        state := flush
        xReg := reqXReg
        yReg := reqYReg
        frameAngleReg := reqAngleReg
      }
    }
    is (done) {
      when (pipeCnt =/= 16.U) {
        pipeCnt := pipeCnt + 1.U
        fbWriter.io.req.valid := true.B
      } .otherwise {
        fbWriter.io.req.valid := false.B
        when (io.vblank) {
          fbIdx := 1.U - fbIdx
          frameAngleReg := currAngleReg
          pipeCnt := 0.U
          state := flush
        }
      }
    }
  }
}
