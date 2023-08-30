// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class VgaSignal extends Module {
  val io = IO(new Bundle {
    val pos = Output(UVec2())
    val hsync = Output(Bool())
    val vsync = Output(Bool())
    val active = Output(Bool())
  })

  val posReg = RegInit(ScreenPos())
  posReg.x := posReg.x + 1.U
  when (posReg.x === (Screen.hbackEnd - 1).U) {
    posReg.x := 0.U
    posReg.y := posReg.y + 1.U
    when (posReg.y === (Screen.vbackEnd - 1).U) {
      posReg.y := 0.U
    }
  }
  io.pos := posReg

  io.hsync := Screen.hfrontEnd.U <= posReg.x && posReg.x < Screen.hsyncEnd.U
  io.vsync := Screen.vfrontEnd.U <= posReg.y && posReg.y < Screen.vsyncEnd.U
  io.active := posReg.x < Screen.width.U && posReg.y < Screen.height.U
}
