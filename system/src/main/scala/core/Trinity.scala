// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Trinity extends Module {
  val io = IO(new Bundle {
    val ddr3 = new Ddr3Ext
    val vga  = new VgaExt
  })

  val clkWiz = Module(new ClkWiz)
  val vram   = Module(new Vram)
  io.ddr3 <> vram.io.ddr3

  val fbSwapper    = Module(new FbSwapper)
  val displayVsync = Wire(Bool())
  val graphicsDone = Wire(Bool())
  fbSwapper.io.displayVsync := RegNext(RegNext(displayVsync))
  fbSwapper.io.graphicsDone := RegNext(RegNext(graphicsDone))

  val displaySysRst = Module(new ProcSysRst)
  displaySysRst.clock  := clkWiz.io.clkDisplay
  vram.io.aclkDisplay  := clkWiz.io.clkDisplay
  vram.io.arstnDisplay := displaySysRst.io.periArstn
  withClockAndReset(clkWiz.io.clkDisplay, displaySysRst.io.periRst) {
    val display = Module(new Display)
    vram.io.axiDisplay <> display.io.vram
    io.vga             <> display.io.vga
    display.io.fbId := RegNext(RegNext(fbSwapper.io.displayFbId))
    displayVsync    := RegNext(display.io.vga.vsync === VgaTiming.polarity.B, false.B)
  }

  val graphicsSysRst = Module(new ProcSysRst)
  graphicsSysRst.clock  := clkWiz.io.clkGraphics
  vram.io.aclkGraphics  := clkWiz.io.clkGraphics
  vram.io.arstnGraphics := graphicsSysRst.io.periArstn
  withClockAndReset(clkWiz.io.clkGraphics, graphicsSysRst.io.periRst) {
    val renderer = Module(new Renderer)
    vram.io.axiGraphics <> renderer.io.vram
    renderer.io.fbId := RegNext(RegNext(fbSwapper.io.graphicsFbId))
    graphicsDone     := RegNext(renderer.io.done, false.B)
  }
}

object Trinity extends App {
  emitVerilog(new Trinity, Array("--target-dir", "generated"))
}
