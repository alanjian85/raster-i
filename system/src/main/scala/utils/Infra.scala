// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class clk_wiz extends BlackBox {
  val io = IO(new Bundle {
    val clk          = Input(Bool())
    val clk_graphics = Output(Bool())
    val clk_display  = Output(Bool())
  })
}

class ClkWiz extends Module {
  val io = IO(new Bundle {
    val clkGraphics = Output(Clock())
    val clkDisplay  = Output(Clock())
  })

  val clkWiz = Module(new clk_wiz)
  clkWiz.io.clk  := clock.asBool
  io.clkGraphics := clkWiz.io.clk_graphics.asClock
  io.clkDisplay  := clkWiz.io.clk_display.asClock
}

class proc_sys_rst extends BlackBox {
  val io = IO(new Bundle {
    val slowest_sync_clk     = Input(Bool())
    val ext_reset_in         = Input(Bool())
    val aux_reset_in         = Input(Bool())
    val mb_debug_sys_rst     = Input(Bool())
    val dcm_locked           = Input(Bool())
    val mb_reset             = Output(Bool())
    val bus_struct_reset     = Output(Bool())
    val peripheral_reset     = Output(Bool())
    val interconnect_aresetn = Output(Bool())
    val peripheral_aresetn   = Output(Bool())
  })
}

class ProcSysRst extends Module {
    val io = IO(new Bundle {
        val periRst   = Output(Reset())
        val periArstn = Output(Reset())
    })

    val procSysRst = Module(new proc_sys_rst)
    procSysRst.io.slowest_sync_clk := clock.asBool
    procSysRst.io.ext_reset_in     := reset.asBool
    procSysRst.io.aux_reset_in     := false.B
    procSysRst.io.mb_debug_sys_rst := false.B
    procSysRst.io.dcm_locked       := true.B
    io.periRst   := procSysRst.io.peripheral_reset
    io.periArstn := procSysRst.io.peripheral_aresetn
}
