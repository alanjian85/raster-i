// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
    val io = IO(new Bundle {
        val axi = new Axi
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

    val recvIdxReg = RegInit(0.U(log2Up(scanSize).W))
    io.axi.rready := true.B
    when (io.axi.rvalid) {
        recvIdxReg := recvIdxReg + 1.U
        when (recvIdxReg === (scanSize - 1).U) {
            recvIdxReg := 0.U
        }
        val rdData = io.axi.rdata
        val wrData = rdData(119, 116) ## rdData(111, 108) ## rdData(103, 100) ##
                     rdData( 87,  84) ## rdData( 79,  76) ## rdData( 71,  68) ##
                     rdData( 55,  52) ## rdData( 47,  44) ## rdData( 39,  36) ##
                     rdData( 23,  20) ## rdData( 15,  12) ## rdData(  7,   4)
        scanBuffer.write(recvIdxReg, wrData)
    }

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
    when (rdValidReg && io.axi.arready) {
        rdValidReg := false.B
    }
    io.axi.araddr := lineIdxReg * scanSize.U << 4.U
    io.axi.arburst := "b01".U
    io.axi.arcache := 0.U
    io.axi.arlen := (scanSize - 1).U
    io.axi.arlock := false.B
    io.axi.arprot := 0.U
    io.axi.arqos := 0.U
    io.axi.arregion := 0.U
    io.axi.arsize := "b100".U
    io.axi.arvalid := rdValidReg

    io.axi.awaddr := 0.U
    io.axi.awburst := 0.U
    io.axi.awcache := 0.U
    io.axi.awlen := 0.U
    io.axi.awlock := false.B
    io.axi.awprot := 0.U
    io.axi.awqos := 0.U
    io.axi.awregion := 0.U
    io.axi.awsize := 0.U
    io.axi.awvalid := false.B
  
    io.axi.wdata := 0.U
    io.axi.wlast := false.B
    io.axi.wstrb := 0.U
    io.axi.wvalid := false.B

    io.axi.bready := false.B
}