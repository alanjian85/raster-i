// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class ImgRom extends BlackBox {
    val io = IO(new Bundle {
        val clka = Input(Clock())
        val addra = Input(UInt(14.W))
        val douta = Output(UInt(128.W))
    })
}