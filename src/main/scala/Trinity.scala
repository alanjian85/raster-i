// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class TrinityIO extends Bundle {
  val r = Output(UInt(4.W))
  val g = Output(UInt(4.W))
  val b = Output(UInt(4.W))
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
    shader.io.x := vgaSignal.io.x
    shader.io.y := vgaSignal.io.y
    io.r := Mux(vgaSignal.io.active, shader.io.r, "h0".U)
    io.g := Mux(vgaSignal.io.active, shader.io.g, "h0".U)
    io.b := Mux(vgaSignal.io.active, shader.io.b, "h0".U)
  }
}
