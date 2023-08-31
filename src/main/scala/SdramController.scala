// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class SdramController extends BlackBox {
    val io = IO(new Bundle {
        val clock = Input(Clock())
        val reset = Input(Bool())

        val axi0 = Flipped(new Axi)
        val clock0 = Input(Clock())
        val aresetn0 = Input(Bool())

        val axi1 = Flipped(new Axi)
        val clock1 = Input(Clock())
        val aresetn1 = Input(Bool())

        val ddr3 = new Ddr3
    })
}