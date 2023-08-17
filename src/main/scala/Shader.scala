// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Shader extends Module {
  val io = IO(new Bundle {
    val x = Input(UInt(11.W))
    val y = Input(UInt(11.W))
    val r = Output(UInt(4.W))
    val g = Output(UInt(4.W))
    val b = Output(UInt(4.W))
  })

  val sx = io.x.asSInt - 400.S
  val sy = io.y.asSInt - 300.S
  val r = sx * sx + sy * sy
  val visible = r <= 10000.S
  io.r := Mux(visible, "hf".U, "h1".U)
  io.g := Mux(visible, "hf".U, "h3".U)
  io.b := Mux(visible, "hf".U, "h7".U)
}
