// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Shader extends Module {
  val io = IO(new Bundle {
    val x = Input(UInt(log2Up(Screen.width).W))
    val y = Input(UInt(log2Up(Screen.height).W))
    val pix = Output(UInt(32.W))
  })

  val diffX = io.x.zext - 512.S
  val diffY = io.y.zext - 384.S
  val r = diffX * diffX + diffY * diffY
  when (r <= 10000.S) {
    io.pix := "hffffffff".U
  } .otherwise {
    io.pix := "hff703010".U
  }
}
