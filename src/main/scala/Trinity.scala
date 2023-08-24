// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class TrinityIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val ddr3 = new Ddr3PhyIO
}

class Trinity extends Module {
  val io = IO(new TrinityIO)

  val clockingWizard = Module(new ClockingWizard)
  clockingWizard.io.clock := clock

  val sdramController = Module(new SdramController)
  io.ddr3 <> sdramController.io.ddr3
  
  sdramController.io.app_addr      := 0.U
  sdramController.io.app_cmd       := 0.U
  sdramController.io.app_en        := false.B
  sdramController.io.app_wdf_data  := 0.U
  sdramController.io.app_wdf_end   := false.B
  sdramController.io.app_wdf_wren  := false.B
  sdramController.io.app_sr_req    := false.B
  sdramController.io.app_ref_req   := false.B
  sdramController.io.app_zq_req    := false.B
  sdramController.io.app_wdf_mask  := 0.U

  val resetSync = RegNext(RegNext(reset))
  sdramController.io.sys_clk_i := clockingWizard.io.sys_clk
  sdramController.io.clk_ref_i := clockingWizard.io.clk_ref
  sdramController.io.sys_rst   := resetSync

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
