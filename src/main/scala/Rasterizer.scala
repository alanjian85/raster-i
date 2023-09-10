// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Rasterizer extends Module {
  val io = IO(new Bundle {
    val ax = Input(UInt())
    val ay = Input(UInt())
    val bx = Input(UInt())
    val by = Input(UInt())
    val cx = Input(UInt())
    val cy = Input(UInt())
    val px = Input(UInt())
    val py = Input(UInt())
    val visible = Output(Bool())
    val u = Output(UInt())
    val v = Output(UInt())
    val w = Output(UInt())
    val a = Output(UInt())
  })

  val abx = io.bx.zext - io.ax.zext
  val aby = io.by.zext - io.ay.zext
  val acx = io.cx.zext - io.ax.zext
  val acy = io.cy.zext - io.ay.zext 
  val sa = abx * acy - aby * acx

  val apx = io.px.zext - io.ax.zext
  val apy = io.py.zext - io.ay.zext
  val sv = apx * acy - apy * acx
  val sw = abx * apy - aby * apx

  val a = sa.abs
  val v = Mux(sa >= 0.S, sv, -sv)
  val w = Mux(sa >= 0.S, sw, -sw)
  val u = a - v - w
  io.visible := (u >= 0.S) && (v >= 0.S) && (w >= 0.S) && (a =/= 0.S)
  io.u := u.asUInt
  io.v := v.asUInt
  io.w := w.asUInt
  io.a := a.asUInt
}
