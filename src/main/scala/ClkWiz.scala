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
