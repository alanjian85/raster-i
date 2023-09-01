// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class TrinityIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val ddr3 = new Ddr3
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
    val addrMax = Screen.width * Screen.height >> 2
    val addrReg = RegInit(0.U(log2Up(addrMax).W))
    val addrValidReg = RegInit(true.B)
    when (addrValidReg && sdramController.io.axi0.awready) {
      addrReg := addrReg + 1.U
      when (addrReg === (addrMax - 1).U) {
        addrReg := 0.U
      }
      addrValidReg := false.B
    }

    sdramController.io.axi0.awaddr := addrReg << 4.U
    sdramController.io.axi0.awburst := 0.U
    sdramController.io.axi0.awcache := 0.U
    sdramController.io.axi0.awlen := 0.U
    sdramController.io.axi0.awlock := false.B
    sdramController.io.axi0.awprot := 0.U
    sdramController.io.axi0.awqos := 0.U
    sdramController.io.axi0.awregion := 0.U
    sdramController.io.axi0.awsize := "b100".U
    sdramController.io.axi0.awvalid := addrValidReg

    val dataReg = RegInit(0.U(128.W))
    when (sdramController.io.axi0.wvalid && sdramController.io.axi0.wready) {
      addrValidReg := true.B
      when (addrReg === 60100.U) {
        dataReg := 0.U
      } .otherwise {
        dataReg := ~0.U(128.W)
      }
    }
    sdramController.io.axi0.wdata := dataReg
    sdramController.io.axi0.wlast := true.B
    sdramController.io.axi0.wstrb := "hffff".U
    sdramController.io.axi0.wvalid := !addrValidReg
  
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
    val display = Module(new Display)
    sdramController.io.axi1 <> display.io.axi
    io.pix := display.io.pix
    io.hsync := display.io.hsync
    io.vsync := display.io.vsync
  }
}
