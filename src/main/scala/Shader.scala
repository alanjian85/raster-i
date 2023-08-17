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

  val sx = io.pos.x.asSInt - 400.S
  val sy = io.pos.y.asSInt - 300.S
  val r = sx * sx + sy * sy
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
