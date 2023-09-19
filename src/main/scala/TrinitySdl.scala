// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import chisel3.util.experimental._

class TrinitySdl extends Module {
  val io = IO(new Bundle {
    val angle = Input(UInt(9.W))
    val x = Input(UInt(log2Up(Screen.width).W))
    val y = Input(UInt(log2Up(Screen.height).W))
    val pix = Output(UInt(32.W))
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

  val rasterizer = Module(new Rasterizer) 
  rasterizer.io.ax := RegNext(vertShader.io.ax)
  rasterizer.io.ay := RegNext(vertShader.io.ay)
  rasterizer.io.bx := RegNext(vertShader.io.bx)
  rasterizer.io.by := RegNext(vertShader.io.by)
  rasterizer.io.bz := RegNext(vertShader.io.bz)
  rasterizer.io.cx := RegNext(vertShader.io.cx)
  rasterizer.io.cy := RegNext(vertShader.io.cy)
  rasterizer.io.cz := RegNext(vertShader.io.cz)
  rasterizer.io.px := RegNext(RegNext(RegNext(io.x)))
  rasterizer.io.py := RegNext(RegNext(RegNext(io.y)))

  val fragShader = Module(new FragShader)
  fragShader.io.visible := RegNext(rasterizer.io.visible)
  fragShader.io.u := RegNext(rasterizer.io.u) 
  fragShader.io.v := RegNext(rasterizer.io.v)
  fragShader.io.w := RegNext(rasterizer.io.w)
  fragShader.io.a := RegNext(rasterizer.io.a)

  val ditherer = Module(new Ditherer)
  ditherer.io.px := RegNext(RegNext(RegNext(rasterizer.io.px)))
  ditherer.io.py := RegNext(RegNext(RegNext(rasterizer.io.py)))
  ditherer.io.inPix := RegNext(fragShader.io.pix)

  io.pix := ditherer.io.outPix
}
