// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import scala.io.Source

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
    
  val ddrCtrl = Module(new DdrCtrl)
  io.ddr3 <> ddrCtrl.io.ddr3

  val renderSystemReset = Module(new ProcessorSystemReset)
  renderSystemReset.io.slowest_sync_clk := clock
  renderSystemReset.io.ext_reset_in := reset
  renderSystemReset.io.aux_reset_in := false.B
  renderSystemReset.io.mb_debug_sys_rst := false.B
  renderSystemReset.io.dcm_locked := true.B

  ddrCtrl.io.aclkRender := clock
  ddrCtrl.io.arstnRender := renderSystemReset.io.peripheral_aresetn

  withClockAndReset(renderSystemReset.io.slowest_sync_clk, renderSystemReset.io.peripheral_reset) {
    val imgRom = Module(new ImgRom)
    imgRom.io.clka := renderSystemReset.io.slowest_sync_clk

    ddrCtrl.io.axiRender.addr.bits.id := DontCare
    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    ddrCtrl.io.axiRender.addr.bits.addr := (yReg * Screen.width.U + xReg) << 2.U
    ddrCtrl.io.axiRender.addr.bits.burst := 0.U
    ddrCtrl.io.axiRender.addr.bits.len := 0.U
    ddrCtrl.io.axiRender.addr.bits.size := "b100".U
    val addrValidReg = RegInit(true.B)    
    ddrCtrl.io.axiRender.addr.valid := addrValidReg
    when (ddrCtrl.io.axiRender.addr.valid && ddrCtrl.io.axiRender.addr.ready) {
      addrValidReg := false.B
    }

    imgRom.io.addra := (yReg >> 2.U) * (Screen.width >> 4).U + (xReg >> 4.U)
    ddrCtrl.io.axiRender.data.bits.data := imgRom.io.douta
    switch (xReg(3, 2)) {
      is(0.U) {
        ddrCtrl.io.axiRender.data.bits.data := Fill(4, imgRom.io.douta(31, 0))
      }
      is(1.U) {
        ddrCtrl.io.axiRender.data.bits.data := Fill(4, imgRom.io.douta(63, 32))
      }
      is(2.U) {
        ddrCtrl.io.axiRender.data.bits.data := Fill(4, imgRom.io.douta(95, 64))
      }
      is(3.U) {
        ddrCtrl.io.axiRender.data.bits.data := Fill(4, imgRom.io.douta(127, 96))
      }
    }
    ddrCtrl.io.axiRender.data.bits.last := true.B
    ddrCtrl.io.axiRender.data.bits.strb := "hffff".U
    ddrCtrl.io.axiRender.data.valid := !addrValidReg
    when (ddrCtrl.io.axiRender.data.valid && ddrCtrl.io.axiRender.data.ready) {
      xReg := xReg + 4.U
      when (xReg === (Screen.width - 4).U) {
        xReg := 0.U
        yReg := yReg + 1.U
        when (yReg === (Screen.height - 1).U) {
          yReg := 0.U
        }
      }
      addrValidReg := true.B
    }
  
    ddrCtrl.io.axiRender.resp.ready  := true.B
  }

  val displaySystemReset = Module(new ProcessorSystemReset)
  displaySystemReset.io.slowest_sync_clk := clockingWizard.io.pixelClock
  displaySystemReset.io.ext_reset_in := reset
  displaySystemReset.io.aux_reset_in := false.B
  displaySystemReset.io.mb_debug_sys_rst := false.B
  displaySystemReset.io.dcm_locked := true.B

  ddrCtrl.io.aclkDisplay := clockingWizard.io.pixelClock
  ddrCtrl.io.arstnDisplay := displaySystemReset.io.peripheral_aresetn

  withClockAndReset(displaySystemReset.io.slowest_sync_clk, displaySystemReset.io.peripheral_reset) {
    val display = Module(new Display)
    ddrCtrl.io.axiDisplay <> display.io.axi
    io.pix := display.io.pix
    io.hsync := display.io.hsync
    io.vsync := display.io.vsync
  }
}
