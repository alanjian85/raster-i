// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Axi extends Bundle {
    val araddr = Output(UInt(28.W))
    val arburst = Output(UInt(2.W))
    val arcache = Output(UInt(4.W))
    val arlen = Output(UInt(8.W))
    val arlock = Output(Bool())
    val arprot = Output(UInt(3.W))
    val arqos = Output(UInt(4.W))
    val arready = Input(Bool())
    val arregion = Output(UInt(4.W))
    val arsize = Output(UInt(3.W))
    val arvalid = Output(Bool())

    val awaddr = Output(UInt(28.W))
    val awburst = Output(UInt(2.W))
    val awcache = Output(UInt(4.W))
    val awlen = Output(UInt(8.W))
    val awlock = Output(Bool())
    val awprot = Output(UInt(3.W))
    val awqos = Output(UInt(4.W))
    val awready = Input(Bool())
    val awregion = Output(UInt(4.W))
    val awsize = Output(UInt(3.W))
    val awvalid = Output(Bool())

    val bready = Output(Bool())
    val bresp = Input(UInt(2.W))
    val bvalid = Input(Bool())

    val rdata = Input(UInt(128.W))
    val rlast = Input(Bool())
    val rready = Output(Bool())
    val rresp = Input(UInt(2.W))
    val rvalid = Input(Bool())

    val wdata = Output(UInt(128.W))
    val wlast = Output(Bool())
    val wready = Input(Bool())
    val wstrb = Output(UInt(16.W))
    val wvalid = Output(Bool())
}