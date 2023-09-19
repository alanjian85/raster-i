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

  //def div

  //val r = Mux(io.visible, div(io.u * 255.U, io.a), "h00".U)
  //val g = Mux(io.visible, div(io.v * 255.U, io.a), "h00".U)
  //val b = Mux(io.visible, div(io.w * 255.U, io.a), "h00".U)
  val r = Mux(io.visible, "hff".U(8.W), "h00".U)
  val g = Mux(io.visible, "h00".U(8.W), "h00".U)
  val b = Mux(io.visible, "h00".U(8.W), "h00".U)
  io.pix := "hff".U ## b ## g ## r
}
