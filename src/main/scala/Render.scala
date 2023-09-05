// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Render extends Module {
    val io = IO(new Bundle {
        val axi = new WrAxi(28, 128)
        val done = Output(Bool())
    })

    val imgRom = Module(new ImgRom)

    io.axi.addr.bits.id := DontCare
    val activeReg = RegInit(true.B)
    io.done := !activeReg
    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    io.axi.addr.bits.addr := (yReg * Screen.width.U + xReg) << 2.U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.bits.len := 0.U
    io.axi.addr.bits.size := "b100".U
    val addrValidReg = RegInit(true.B)    
    io.axi.addr.valid := addrValidReg && activeReg
    when (io.axi.addr.valid && io.axi.addr.ready) {
      addrValidReg := false.B
    }

    imgRom.io.addr := (yReg >> 2.U) * (Screen.width >> 4).U + (xReg >> 4.U)
    io.axi.data.bits.data := imgRom.io.data
    switch (xReg(3, 2)) {
      is(0.U) {
        io.axi.data.bits.data := Fill(4, imgRom.io.data(31, 0))
      }
      is(1.U) {
        io.axi.data.bits.data := Fill(4, imgRom.io.data(63, 32))
      }
      is(2.U) {
        io.axi.data.bits.data := Fill(4, imgRom.io.data(95, 64))
      }
      is(3.U) {
        io.axi.data.bits.data := Fill(4, imgRom.io.data(127, 96))
      }
    }
    io.axi.data.bits.last := true.B
    io.axi.data.bits.strb := "hffff".U
    io.axi.data.valid := !addrValidReg && activeReg
    when (io.axi.data.valid && io.axi.data.ready) {
      xReg := xReg + 4.U
      when (xReg === (Screen.width - 4).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          activeReg := false.B
        }
      }
      addrValidReg := true.B
    }
  
    io.axi.resp.ready  := true.B
}