// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class RGB444 extends Bundle {
  val r = UInt(4.W)
  val g = UInt(4.W)
  val b = UInt(4.W)
}

class Shader extends Module {
  val io = IO(new Bundle {
    val pos = Input(new ScreenPos())
    val pix = Output(new RGB444())
  })

  val diff = io.pos.asSVec2 - ScreenPosInit(400.U, 300.U).asSVec2
  val r = diff.x * diff.x + diff.y * diff.y
  val visible = r <= 10000.S
  when (visible) {
    io.pix.r := "hf".U
    io.pix.g := "hf".U
    io.pix.b := "hf".U
  } .otherwise {
    io.pix.r := "h1".U
    io.pix.g := "h3".U
    io.pix.b := "h7".U
  }
}
