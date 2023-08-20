// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

object ScreenPosInit {
  def apply(x: UInt = 0.U) = {
    val xTemp = Wire(UInt(11.W))
    xTemp := x
    UVec2Init(xTemp)
  }

  def apply(x: UInt, y: UInt) = {
    val xTemp = Wire(UInt(11.W))
    xTemp := x
    val yTemp = Wire(UInt(11.W))
    yTemp := y
    UVec2Init(xTemp, yTemp)
  }
}

class VgaSignal extends Module {
  val io = IO(new Bundle {
    val pos = Output(UVec2())
    val hsync = Output(Bool())
    val vsync = Output(Bool())
    val active = Output(Bool())
  })

  val HA_END = 800
  val HF_END = HA_END + 40
  val HS_END = HF_END + 128
  val HB_END = HS_END + 88

  val VA_END = 600
  val VF_END = VA_END + 1
  val VS_END = VF_END + 4
  val VB_END = VS_END + 23

  val posReg = RegInit(ScreenPosInit())
  posReg.x := posReg.x + 1.U
  when (posReg.x === (HB_END - 1).U) {
    posReg.x := 0.U
    posReg.y := posReg.y + 1.U
  }
  when (posReg.y === (VB_END - 1).U) {
    posReg.y := 0.U
  }
  io.pos := posReg

  io.hsync := HF_END.U <= posReg.x && posReg.x < HS_END.U
  io.vsync := VF_END.U <= posReg.y && posReg.y < VS_END.U
  io.active := posReg.x < HA_END.U && posReg.y < VA_END.U
}
