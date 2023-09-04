// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class img_rom extends BlackBox {
    val io = IO(new Bundle {
        val clka = Input(Bool())
        val addra = Input(UInt(14.W))
        val douta = Output(UInt(128.W))
    })
}

class ImgRom extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(14.W))
        val data = Output(UInt(128.W))
    })

    val imgRom = Module(new img_rom)
    imgRom.io.clka := clock.asBool
    imgRom.io.addra := io.addr
    io.data := imgRom.io.douta
}