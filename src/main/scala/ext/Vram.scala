// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.experimental._

class Ddr3Ext extends Bundle {
  val addr    = Output(UInt(14.W))
  val ba      = Output(UInt(3.W))
  val cas_n   = Output(Bool())
  val ck_n    = Output(UInt(1.W))
  val ck_p    = Output(UInt(1.W))
  val cke     = Output(UInt(1.W))
  val cs_n    = Output(UInt(1.W))
  val dm      = Output(UInt(2.W))
  val dq      = Analog(16.W)
  val dqs_n   = Analog(2.W)
  val dqs_p   = Analog(2.W)
  val odt     = Output(UInt(1.W))
  val ras_n   = Output(Bool())
  val reset_n = Output(Bool())
  val we_n    = Output(Bool())
}

object Vram {
  val addrWidth = 28
  val dataWidth = 128
}

class vram extends BlackBox {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val reset = Input(Bool())

    val graphics_axi     = new WrAxiExt(Vram.addrWidth, Vram.dataWidth)
    val graphics_aclk    = Input(Bool())
    val graphics_aresetn = Input(Bool())

    val display_axi     = new RdAxiExt(Vram.addrWidth, Vram.dataWidth)
    val display_aclk    = Input(Bool())
    val display_aresetn = Input(Bool())

    val ddr3 = new Ddr3Ext
  })
}

class Vram extends Module {
  val io = IO(new Bundle {
    val axiGraphics = Flipped(new WrAxi(Vram.addrWidth, Vram.dataWidth))
    val aclkGraphics = Input(Clock())
    val arstnGraphics = Input(Reset())

    val axiDisplay = Flipped(new RdAxi(Vram.addrWidth, Vram.dataWidth))
    val aclkDisplay = Input(Clock())
    val arstnDisplay = Input(Reset())

    val ddr3 = new Ddr3Ext
  })

  val vram = Module(new vram)
  vram.io.clk := clock.asBool
  vram.io.reset := reset.asBool

  vram.io.graphics_axi.connect(io.axiGraphics)
  vram.io.graphics_aclk := io.aclkGraphics.asBool
  vram.io.graphics_aresetn := io.arstnGraphics.asBool

  vram.io.display_axi.connect(io.axiDisplay)
  vram.io.display_aclk := io.aclkDisplay.asBool
  vram.io.display_aresetn := io.arstnDisplay.asBool

  io.ddr3 <> vram.io.ddr3
}
