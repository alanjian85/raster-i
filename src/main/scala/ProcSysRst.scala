// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class proc_sys_rst extends BlackBox { 
    val io = IO(new Bundle {
        val slowest_sync_clk = Input(Clock())
        val ext_reset_in = Input(Bool())
        val aux_reset_in = Input(Bool())
        val mb_debug_sys_rst = Input(Bool())
        val dcm_locked = Input(Bool())
        val mb_reset = Output(Bool())
        val bus_struct_reset = Output(Bool())
        val peripheral_reset = Output(Bool())
        val interconnect_aresetn = Output(Bool())
        val peripheral_aresetn = Output(Bool())
    })
}

class ProcSysRst extends Module {
    val io = IO(new Bundle {
        val rst = Output(Bool())
        val arstn = Output(Bool())
    })

    val procSysRst = Module(new proc_sys_rst)
    procSysRst.io.slowest_sync_clk := clock
    procSysRst.io.ext_reset_in := reset
    procSysRst.io.aux_reset_in := false.B
    procSysRst.io.mb_debug_sys_rst := false.B
    procSysRst.io.dcm_locked := true.B
    io.rst := procSysRst.io.peripheral_reset
    io.arstn := procSysRst.io.peripheral_aresetn
}