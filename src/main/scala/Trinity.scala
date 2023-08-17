// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class TrinityIO extends Bundle {
  val pix = Output(new RGB444())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
}

class Trinity extends Module {
  val io = IO(new TrinityIO())

  val pixelClock = Module(new PixelClock())
  pixelClock.io.clock := clock
  withClockAndReset(pixelClock.io.clk_pix, reset) {
    val vgaSignal = Module(new VgaSignal())
    io.hsync := vgaSignal.io.hsync
    io.vsync := vgaSignal.io.vsync
    val shader = Module(new Shader())
    shader.io.pos := vgaSignal.io.pos
    when (vgaSignal.io.active) {
      io.pix := shader.io.pix
    } .otherwise {
      io.pix.r := 0.U
      io.pix.g := 0.U
      io.pix.b := 0.U
    }
  }
}
