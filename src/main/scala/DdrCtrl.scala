// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.experimental._

class Ddr3 extends Bundle {
    val addr           = Output(UInt(14.W))
    val ba             = Output(UInt(3.W))
    val cas_n          = Output(Bool())
    val ck_n           = Output(UInt(1.W))
    val ck_p           = Output(UInt(1.W))
    val cke            = Output(UInt(1.W))
    val ras_n          = Output(Bool())
    val reset_n        = Output(Bool())
    val we_n           = Output(Bool())
    val dq             = Analog(16.W)
    val dqs_n          = Analog(2.W)
    val dqs_p          = Analog(2.W)
    val cs_n           = Output(UInt(1.W))
    val dm             = Output(UInt(2.W))
    val odt            = Output(UInt(1.W)) 
}

class ddr_ctrl extends BlackBox {
    val io = IO(new Bundle {
        val clk = Input(Clock())
        val rst = Input(Reset())

        val render_axi = new WrAxiExt(28, 128)
        val render_aclk = Input(Clock())
        val render_aresetn = Input(Reset())

        val display_axi = new RdAxiExt(28, 128)
        val display_aclk = Input(Clock())
        val display_aresetn = Input(Reset())

        val ddr3 = new Ddr3
    })
}

class DdrCtrl extends Module {
    val io = IO(new Bundle {
        val axiRender = Flipped(new WrAxi(28, 128))
        val aclkRender = Input(Clock())
        val arstnRender = Input(Reset())

        val axiDisplay = Flipped(new RdAxi(28, 128))
        val aclkDisplay = Input(Clock())
        val arstnDisplay = Input(Reset())

        val ddr3 = new Ddr3
    })

    val ddrCtrl = Module(new ddr_ctrl)
    ddrCtrl.io.clk := clock
    ddrCtrl.io.rst := reset

    ddrCtrl.io.render_axi.connect(io.axiRender)
    ddrCtrl.io.render_aclk := io.aclkRender
    ddrCtrl.io.render_aresetn := io.arstnRender

    ddrCtrl.io.display_axi.connect(io.axiDisplay)
    ddrCtrl.io.display_aclk := io.aclkDisplay
    ddrCtrl.io.display_aresetn := io.arstnDisplay

    ddrCtrl.io.ddr3 <> io.ddr3
}