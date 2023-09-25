// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class TrinityIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val ddr3 = new Ddr3Ext
}

class Trinity extends Module {
  val io = IO(new TrinityIO)

  val clkWiz = Module(new ClkWiz)
  val ddrCtrl = Module(new DdrCtrl)
  io.ddr3 <> ddrCtrl.io.ddr3

  val dispFbIdx = Wire(UInt(1.W))
  val vblank = Wire(Bool())

  val renderSysRst = Module(new ProcSysRst)
  renderSysRst.clock := clkWiz.io.clkRender
  ddrCtrl.io.aclkRender := clkWiz.io.clkRender
  ddrCtrl.io.arstnRender := renderSysRst.io.arstn
  withClockAndReset(clkWiz.io.clkRender, renderSysRst.io.rst) {
    val render = Module(new Render)
    render.io.vblank := RegNext(RegNext(vblank))
    ddrCtrl.io.axiRender <> render.io.axi
    dispFbIdx := 1.U - render.io.fbIdx
  }

  val displaySysRst = Module(new ProcSysRst)
  displaySysRst.clock := clkWiz.io.clkDisplay
  ddrCtrl.io.aclkDisplay := clkWiz.io.clkDisplay
  ddrCtrl.io.arstnDisplay := displaySysRst.io.arstn
  withClockAndReset(clkWiz.io.clkDisplay, displaySysRst.io.rst) {
    val display = Module(new Display)
    display.io.fbIdx := RegNext(RegNext(dispFbIdx))
    ddrCtrl.io.axiDisplay <> display.io.axi
    io.pix := display.io.pix
    io.hsync := display.io.hsync
    io.vsync := display.io.vsync
    vblank := display.io.vblank
  }
}
