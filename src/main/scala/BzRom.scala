// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class bz_rom extends BlackBox {
    val io = IO(new Bundle {
        val clka = Input(Bool())
        val addra = Input(UInt(9.W))
        val douta = Output(UInt(11.W))
    })
}

class BzRom extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(9.W))
        val data = Output(UInt(11.W))
    })

    val bzRom = Module(new bz_rom)
    bzRom.io.clka := clock.asBool
    bzRom.io.addra := io.addr
    io.data := bzRom.io.douta
}