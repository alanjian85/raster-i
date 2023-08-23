// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class TrinitySdlIO extends Bundle {
  val pix = Output(RGB4())
  val hsync = Output(Bool())
  val vsync = Output(Bool())
  val pos = Output(UVec2())
  val active = Output(Bool())
}

class TrinitySdl extends Module {
  val io = IO(new TrinitySdlIO);

  val vgaSignal = Module(new VgaSignal)
  io.pos := vgaSignal.io.pos
  io.hsync := vgaSignal.io.hsync
  io.vsync := vgaSignal.io.vsync
  io.active := vgaSignal.io.active
  val shader = Module(new Shader)
  shader.io.pos := vgaSignal.io.pos
  io.pix := shader.io.pix
}
