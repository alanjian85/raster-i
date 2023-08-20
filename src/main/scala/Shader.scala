// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT
import chisel3._

class Shader extends Module {
  val io = IO(new Bundle {
    val pos = Input(UVec2())
    val pix = Output(RGB4())
  })

  val diff = io.pos.asSVec2 - ScreenPosInit(400.U, 300.U).asSVec2
  val r = diff.x * diff.x + diff.y * diff.y
  val visible = r <= 10000.S
  when (visible) {
    io.pix := RGB4Init("hf".U)
  } .otherwise {
    io.pix := RGB4Init("h1".U, "h3".U, "h7".U)
  }
}
