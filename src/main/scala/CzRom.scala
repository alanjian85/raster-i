// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class cz_rom extends BlackBox {
    val io = IO(new Bundle {
        val clka = Input(Bool())
        val addra = Input(UInt(9.W))
        val douta = Output(UInt(11.W))
    })
}

class CzRom extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(9.W))
        val data = Output(UInt(11.W))
    })

    val czRom = Module(new cz_rom)
    czRom.io.clka := clock.asBool
    czRom.io.addra := io.addr
    io.data := czRom.io.douta
}