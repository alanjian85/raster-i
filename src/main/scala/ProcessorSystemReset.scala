// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class ProcessorSystemReset extends BlackBox { 
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
