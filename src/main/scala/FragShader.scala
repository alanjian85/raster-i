// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FragShader extends Module {
  val io = IO(new Bundle {
    val visible = Input(Bool())
    val u = Input(UInt())
    val v = Input(UInt())
    val w = Input(UInt())
    val a = Input(UInt())
    val pix = Output(UInt(32.W))
  })

  val r = Mux(io.visible, (io.u * 256.U / io.a)(7, 0), "h00".U)
  val g = Mux(io.visible, (io.v * 256.U / io.a)(7, 0), "h00".U)
  val b = Mux(io.visible, (io.w * 256.U / io.a)(7, 0), "h00".U)
  io.pix := "hff".U ## b ## g ## r
}
