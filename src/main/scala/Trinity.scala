// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util.unsignedBitLength

class TrinityIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val ddr3 = new Ddr3PhyIO
  val led = Output(Bool())
}

class Trinity extends Module {
  val io = IO(new TrinityIO)

  val clockingWizard = Module(new ClockingWizard)
  clockingWizard.io.clock := clock

  val sdramController = Module(new SdramController)
  io.ddr3 <> sdramController.io.ddr3
  sdramController.io.app_sr_req    := false.B
  sdramController.io.app_ref_req   := false.B
  sdramController.io.app_zq_req    := false.B
  val resetSync = RegNext(RegNext(reset))
  sdramController.io.sys_clk_i := clockingWizard.io.sys_clk
  sdramController.io.clk_ref_i := clockingWizard.io.clk_ref
  sdramController.io.sys_rst   := resetSync

  withClockAndReset(sdramController.io.ui_clk, sdramController.io.ui_clk_sync_rst) {
    sdramController.io.app_addr      := 0.U
    sdramController.io.app_en        := true.B
    sdramController.io.app_wdf_mask  := 0.U
    
    val wrLedReg = RegInit(false.B)
    sdramController.io.app_wdf_data  := wrLedReg

    val cmdReg = RegInit(0.U(1.W))
    sdramController.io.app_cmd := cmdReg
    when (sdramController.io.app_rdy && cmdReg === 0.U) {
      cmdReg := 1.U
    }

    val wrenReg = RegInit(true.B)
    sdramController.io.app_wdf_wren := wrenReg
    sdramController.io.app_wdf_end  := wrenReg
    when (wrenReg && sdramController.io.app_wdf_rdy) {
      wrenReg := false.B
    }

    val CNT_MAX = (8333_3335 - 1) / 2
    val cntReg = RegInit(0.U(unsignedBitLength(CNT_MAX).W))
    cntReg := cntReg + 1.U
    when (cntReg === (CNT_MAX - 1).U) {
      cntReg := 0.U
      wrLedReg := !wrLedReg

      cmdReg := 0.U
      wrenReg := true.B
    }

    val rdLedReg = RegInit(false.B)
    io.led := rdLedReg
    when (sdramController.io.app_rd_data_valid) {
      rdLedReg := sdramController.io.app_rd_data
    }
  }

  withClock(clockingWizard.io.clk_pix) {
    val resetSync = RegNext(RegNext(reset))
    withReset(resetSync) {
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
}
