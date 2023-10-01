// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class TrinityIO extends Bundle {
  val vga  = new VgaExt
  val ddr3 = new Ddr3Ext
}

class Trinity extends Module {
  val io = IO(new TrinityIO)

  val clkWiz = Module(new ClkWiz)
  val vram = Module(new Vram)
  io.ddr3 <> vram.io.ddr3

  val dispFbIdx = Wire(UInt(1.W))
  val vblank = Wire(Bool())

  val graphicsSysRst = Module(new ProcSysRst)
  graphicsSysRst.clock := clkWiz.io.clkGraphics
  vram.io.aclkGraphics := clkWiz.io.clkGraphics
  vram.io.arstnGraphics := graphicsSysRst.io.arstn
  withClockAndReset(clkWiz.io.clkGraphics, graphicsSysRst.io.rst) {
    val graphics = Module(new Graphics)
    graphics.io.vblank := RegNext(RegNext(vblank))
    vram.io.axiGraphics <> graphics.io.axi
    dispFbIdx := 1.U - graphics.io.fbIdx
  }

  val displaySysRst = Module(new ProcSysRst)
  displaySysRst.clock := clkWiz.io.clkDisplay
  vram.io.aclkDisplay := clkWiz.io.clkDisplay
  vram.io.arstnDisplay := displaySysRst.io.arstn
  withClockAndReset(clkWiz.io.clkDisplay, displaySysRst.io.rst) {
    val display = Module(new Display)
    display.io.fbIdx := RegNext(RegNext(dispFbIdx))
    vram.io.axiDisplay <> display.io.vram
    io.vga := display.io.vga
    vblank := RegNext(display.io.vga.vsync === VgaTiming.polarity.B)
  }
}
