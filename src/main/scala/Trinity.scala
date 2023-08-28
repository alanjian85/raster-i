// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util.unsignedBitLength

class TrinityIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val ddr3 = new Ddr3
  val led = Output(UInt(4.W))
}

class Trinity extends Module {
  val io = IO(new TrinityIO)

  val clockingWizard = Module(new ClockingWizard)
  clockingWizard.io.clock := clock
    
  val sdramController = Module(new SdramController)
  io.ddr3 <> sdramController.io.ddr3

  sdramController.io.clock := clock
  sdramController.io.reset := reset

  val renderSystemReset = Module(new ProcessorSystemReset)
  renderSystemReset.io.slowest_sync_clk := clock
  renderSystemReset.io.ext_reset_in := reset
  renderSystemReset.io.aux_reset_in := false.B
  renderSystemReset.io.mb_debug_sys_rst := false.B
  renderSystemReset.io.dcm_locked := true.B

  sdramController.io.clock0 := clock
  sdramController.io.aresetn0 := renderSystemReset.io.peripheral_aresetn
  
  withClockAndReset(renderSystemReset.io.slowest_sync_clk, renderSystemReset.io.peripheral_reset) {
    sdramController.io.axi0.bready  := true.B

    sdramController.io.axi0.araddr := 0.U
    sdramController.io.axi0.arburst := 0.U
    sdramController.io.axi0.arcache := 0.U
    sdramController.io.axi0.arlen := 0.U
    sdramController.io.axi0.arlock := false.B
    sdramController.io.axi0.arprot := 0.U
    sdramController.io.axi0.arqos := 0.U
    sdramController.io.axi0.arregion := 0.U
    sdramController.io.axi0.arsize := 0.U
    sdramController.io.axi0.arvalid := false.B

    sdramController.io.axi0.rready := false.B

    sdramController.io.axi0.awaddr := 0.U
    sdramController.io.axi0.awburst := 0.U
    sdramController.io.axi0.awcache := 0.U
    sdramController.io.axi0.awlen := 0.U
    sdramController.io.axi0.awlock := false.B
    sdramController.io.axi0.awprot := 0.U
    sdramController.io.axi0.awqos := 0.U
    sdramController.io.axi0.awregion := 0.U
    sdramController.io.axi0.awsize := "b100".U
    sdramController.io.axi0.awvalid := true.B

    val wrLedReg = RegInit(0.U(4.W))
    sdramController.io.axi0.wdata := wrLedReg
    sdramController.io.axi0.wlast := true.B
    sdramController.io.axi0.wstrb := "hffff".U
    sdramController.io.axi0.wvalid := true.B

    val CNT_MAX = (1_0000_0000 - 1) / 2
    val cntReg = RegInit(0.U(unsignedBitLength(CNT_MAX).W))
    cntReg := cntReg + 1.U
    when (cntReg === CNT_MAX.U) {
      wrLedReg := wrLedReg + 1.U
      cntReg := 0.U
    }
  }

  val displaySystemReset = Module(new ProcessorSystemReset)
  displaySystemReset.io.slowest_sync_clk := clockingWizard.io.pixelClock
  displaySystemReset.io.ext_reset_in := reset
  displaySystemReset.io.aux_reset_in := false.B
  displaySystemReset.io.mb_debug_sys_rst := false.B
  displaySystemReset.io.dcm_locked := true.B

  sdramController.io.clock1 := clockingWizard.io.pixelClock
  sdramController.io.aresetn1 := displaySystemReset.io.peripheral_aresetn

  withClockAndReset(displaySystemReset.io.slowest_sync_clk, displaySystemReset.io.peripheral_reset) {
    sdramController.io.axi1.bready  := false.B
    
    sdramController.io.axi1.araddr := 0.U
    sdramController.io.axi1.arburst := 0.U
    sdramController.io.axi1.arcache := 0.U
    sdramController.io.axi1.arlen := 0.U
    sdramController.io.axi1.arlock := false.B
    sdramController.io.axi1.arprot := 0.U
    sdramController.io.axi1.arqos := 0.U
    sdramController.io.axi1.arregion := 0.U
    sdramController.io.axi1.arsize := "b100".U
    sdramController.io.axi1.arvalid := true.B
    
    sdramController.io.axi1.rready := true.B
    val rdLedReg = RegInit(0.U(4.W))
    io.led := rdLedReg
    when (sdramController.io.axi1.rvalid) {
      rdLedReg := sdramController.io.axi1.rdata
    }
  
    sdramController.io.axi1.awaddr := 0.U
    sdramController.io.axi1.awburst := 0.U
    sdramController.io.axi1.awcache := 0.U
    sdramController.io.axi1.awlen := 0.U
    sdramController.io.axi1.awlock := false.B
    sdramController.io.axi1.awprot := 0.U
    sdramController.io.axi1.awqos := 0.U
    sdramController.io.axi1.awregion := 0.U
    sdramController.io.axi1.awsize := 0.U
    sdramController.io.axi1.awvalid := false.B
  
    sdramController.io.axi1.wdata := 0.U
    sdramController.io.axi1.wlast := false.B
    sdramController.io.axi1.wstrb := 0.U
    sdramController.io.axi1.wvalid := false.B

    val vgaSignal = Module(new VgaSignal)
    io.hsync := vgaSignal.io.hsync
    io.vsync := vgaSignal.io.vsync
    val shader = Module(new Shader)
    shader.io.pos := vgaSignal.io.pos
    when (vgaSignal.io.active) {
      io.pix := shader.io.pix
    } .otherwise {
      io.pix := RGB4Init()
    }
  }
}
