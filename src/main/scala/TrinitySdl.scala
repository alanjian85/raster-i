// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import chisel3.util.experimental._

class TrinitySdl extends Module {
  val io = IO(new Bundle {})
/*
  val io = IO(new Bundle {
    val angle = Input(UInt(9.W))
    val x = Input(UInt(log2Up(Screen.width / 4).W))
    val y = Input(UInt(log2Up(Screen.height).W))
    val pix = Output(Vec(4, UInt(32.W)))
    val outX = Output(UInt(log2Up(Screen.width / 4).W))
    val outY = Output(UInt(log2Up(Screen.height).W))
  })

  val cosRom = SyncReadMem(360, SInt(12.W))
  val bzRom  = SyncReadMem(360, UInt(11.W))
  val czRom  = SyncReadMem(360, UInt(11.W))
  loadMemoryFromFile(cosRom, "cos_rom.hex")
  loadMemoryFromFile(bzRom,  "bz_rom.hex")
  loadMemoryFromFile(czRom,  "cz_rom.hex")

  val vertShader = Module(new VertShader)
  vertShader.io.cos  := cosRom.read(io.angle)
  vertShader.io.inBz := bzRom.read(io.angle)
  vertShader.io.inCz := czRom.read(io.angle)

  val rastAxReg = RegNext(vertShader.io.ax)
  val rastAyReg = RegNext(vertShader.io.ay)
  val rastBxReg = RegNext(vertShader.io.bx)
  val rastByReg = RegNext(vertShader.io.by)
  val rastBzReg = RegNext(vertShader.io.bz)
  val rastCxReg = RegNext(vertShader.io.cx)
  val rastCyReg = RegNext(vertShader.io.cy)
  val rastCzReg = RegNext(vertShader.io.cz)
  val rastPxReg = RegNext(RegNext(RegNext(io.x)))
  val rastPyReg = RegNext(RegNext(RegNext(io.y)))

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

  io.pix := ditherer.io.outPix
  io.outX := RegNext(pxReg(11))
  io.outY := RegNext(pyReg(11))
*/
}
