// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
    val io = IO(new Bundle {
        val axi = new RdAxi(28, 128)
        val pix = Output(RGB4())
        val hsync = Output(Bool())
        val vsync = Output(Bool())
    })

    val scanSize = Screen.width >> 2
    val scanBuffer = Mem(scanSize, UInt(48.W))

    val vgaSignal = Module(new VgaSignal)
    val vgaData = scanBuffer.read(vgaSignal.io.pos.x >> 2.U)
    io.pix := RGB4Init(0.U)
    when (vgaSignal.io.active) {
        switch (vgaSignal.io.pos.x & "b11".U) {
            is(0.U) {
                io.pix.r := vgaData(3, 0)
                io.pix.g := vgaData(7, 4)
                io.pix.b := vgaData(11, 8)
            }
            is(1.U) {
                io.pix.r := vgaData(15, 12)
                io.pix.g := vgaData(19, 16)
                io.pix.b := vgaData(23, 20)
            }
            is(2.U) {
                io.pix.r := vgaData(27, 24)
                io.pix.g := vgaData(31, 28)
                io.pix.b := vgaData(35, 32)
            }
            is (3.U) {
                io.pix.r := vgaData(39, 36)
                io.pix.g := vgaData(43, 40)
                io.pix.b := vgaData(47, 44)
            }
        }
    }
    io.hsync := vgaSignal.io.hsync
    io.vsync := vgaSignal.io.vsync

    io.axi.addr.bits.id := DontCare
    val lineIdxReg = RegInit(0.U(log2Up(Screen.height).W))
    val rdValidReg = RegInit(true.B)
    when (vgaSignal.io.active &&
          vgaSignal.io.pos.x === (Screen.width - 1).U)
    {
        lineIdxReg := lineIdxReg + 1.U
        when (lineIdxReg === (Screen.height - 1).U) {
            lineIdxReg := 0.U
        }
        rdValidReg := true.B
    }
    when (rdValidReg && io.axi.addr.ready) {
        rdValidReg := false.B
    }
    io.axi.addr.bits.addr := lineIdxReg * scanSize.U << 4.U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.bits.len := (scanSize - 1).U
    io.axi.addr.bits.size := "b100".U
    io.axi.addr.valid := rdValidReg

    io.axi.data.bits.id := DontCare
    val recvIdxReg = RegInit(0.U(log2Up(scanSize).W))
    io.axi.data.ready := true.B
    when (io.axi.data.valid) {
        recvIdxReg := recvIdxReg + 1.U
        when (recvIdxReg === (scanSize - 1).U) {
            recvIdxReg := 0.U
        }
        val rdData = io.axi.data.bits.data
        val wrData = rdData(119, 116) ## rdData(111, 108) ## rdData(103, 100) ##
                     rdData( 87,  84) ## rdData( 79,  76) ## rdData( 71,  68) ##
                     rdData( 55,  52) ## rdData( 47,  44) ## rdData( 39,  36) ##
                     rdData( 23,  20) ## rdData( 15,  12) ## rdData(  7,   4)
        scanBuffer.write(recvIdxReg, wrData)
    }
}