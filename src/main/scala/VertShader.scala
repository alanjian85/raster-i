// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class VertShader extends Module {
  val io = IO(new Bundle {
    val cos  = Input(SInt(12.W))
    val inBz = Input(UInt(11.W))
    val inCz = Input(UInt(11.W))
    val ax = Output(UInt())
    val ay = Output(UInt())
    val bx = Output(UInt())
    val by = Output(UInt())
    val bz = Output(UInt())
    val cx = Output(UInt())
    val cy = Output(UInt())
    val cz = Output(UInt())
  })

  io.ax := 512.U
  io.ay := 192.U

  val bzCosReg = RegNext(io.inBz.zext * io.cos >> 10)
  io.bx := (512.S - (bzCosReg * 222.S >> 10)).asUInt
  io.by := RegNext(384.U +& (io.inBz * 192.U >> 10))
  io.bz := RegNext(io.inBz)

  val czCosReg = RegNext(io.inCz.zext * io.cos >> 10)
  io.cx := (512.S + (czCosReg * 222.S >> 10)).asUInt
  io.cy := RegNext(384.U +& (io.inCz * 192.U >> 10))
  io.cz := RegNext(io.inCz)
}
