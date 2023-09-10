// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class VertShader extends Module {
  val io = IO(new Bundle {
    val ax = Output(UInt())
    val ay = Output(UInt())
    val bx = Output(UInt())
    val by = Output(UInt())
    val cx = Output(UInt())
    val cy = Output(UInt())
  })

  io.ax := 290.U
  io.ay := 576.U

  io.bx := 734.U
  io.by := 576.U

  io.cx := 512.U
  io.cy := 192.U
}
