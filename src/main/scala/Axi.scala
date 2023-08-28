// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Axi extends Bundle {
    val araddr = Input(UInt(28.W))
    val arburst = Input(UInt(2.W))
    val arcache = Input(UInt(4.W))
    val arlen = Input(UInt(8.W))
    val arlock = Input(Bool())
    val arprot = Input(UInt(3.W))
    val arqos = Input(UInt(4.W))
    val arready = Output(Bool())
    val arregion = Input(UInt(4.W))
    val arsize = Input(UInt(3.W))
    val arvalid = Input(Bool())

    val awaddr = Input(UInt(28.W))
    val awburst = Input(UInt(2.W))
    val awcache = Input(UInt(4.W))
    val awlen = Input(UInt(8.W))
    val awlock = Input(Bool())
    val awprot = Input(UInt(3.W))
    val awqos = Input(UInt(4.W))
    val awready = Output(Bool())
    val awregion = Input(UInt(4.W))
    val awsize = Input(UInt(3.W))
    val awvalid = Input(Bool())

    val bready = Input(Bool())
    val bresp = Output(UInt(2.W))
    val bvalid = Output(Bool())

    val rdata = Output(UInt(128.W))
    val rlast = Output(Bool())
    val rready = Input(Bool())
    val rresp = Output(UInt(2.W))
    val rvalid = Output(Bool())

    val wdata = Input(UInt(128.W))
    val wlast = Input(Bool())
    val wready = Output(Bool())
    val wstrb = Input(UInt(16.W))
    val wvalid = Input(Bool())
}