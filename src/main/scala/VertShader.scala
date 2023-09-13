// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class VertShader extends Module {
  val io = IO(new Bundle {
    val angle = Input(UInt(9.W))
    val ax = Output(UInt())
    val ay = Output(UInt())
    val bx = Output(UInt())
    val by = Output(UInt())
    val bz = Output(UInt())
    val cx = Output(UInt())
    val cy = Output(UInt())
    val cz = Output(UInt())
  })

  val cosRom = Module(new CosRom)
  val bzRom = Module(new BzRom)
  val czRom = Module(new CzRom)
  cosRom.io.addr := io.angle
  bzRom.io.addr := io.angle
  czRom.io.addr := io.angle

  io.ax := 512.U
  io.ay := 192.U

  io.bx := (512.S - ((bzRom.io.data.zext * cosRom.io.data >> 10) * 222.S >> 10)).asUInt
  io.by := 384.U +& (bzRom.io.data * 192.U >> 10)
  io.bz := bzRom.io.data

  io.cx := (512.S + ((czRom.io.data.zext * cosRom.io.data >> 10) * 222.S >> 10)).asUInt
  io.cy := 384.U +& (czRom.io.data * 192.U >> 10)
  io.cz := czRom.io.data
}
