// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class clk_wiz extends BlackBox {
    val io = IO(new Bundle {
        val clk = Input(Clock())
        val clk_render = Output(Clock())
        val clk_display = Output(Clock())
    })
}

class ClkWiz extends Module { 
    val io = IO(new Bundle {
        val clkRender = Output(Clock())
        val clkDisplay = Output(Clock())
    })

    val clkWiz = Module(new clk_wiz)
    clkWiz.io.clk := clock
    io.clkRender := clkWiz.io.clk_render
    io.clkDisplay := clkWiz.io.clk_display
}
