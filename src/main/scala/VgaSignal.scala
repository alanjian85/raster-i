// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class VgaSignal extends Module {
  val io = IO(new Bundle {
    val x = Output(UInt(11.W))
    val y = Output(UInt(11.W))
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

  val xReg = RegInit(0.U(11.W))
  val yReg = RegInit(0.U(11.W))
  xReg := xReg + 1.U
  when (xReg === (HB_END - 1).U) {
    xReg := 0.U
    yReg := yReg + 1.U
  }
  when (yReg === (VB_END - 1).U) {
    yReg := 0.U
  }
  io.x := xReg
  io.y := yReg

  io.hsync := HF_END.U <= xReg && xReg < HS_END.U
  io.vsync := VF_END.U <= yReg && yReg < VS_END.U
  io.active := xReg < HA_END.U && yReg < VA_END.U
}
