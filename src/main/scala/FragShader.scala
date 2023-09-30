// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FragShader extends Module {
  val barLen = unsignedBitLength(VgaTiming.width * VgaTiming.height) - 1
  val io = IO(new Bundle {
    val inVis = Input(Bool())
    val u = Input(UInt(barLen.W))
    val v = Input(UInt(barLen.W))
    val w = Input(UInt(barLen.W))
    val a = Input(UInt(barLen.W))
    val pix = Output(UInt(32.W))
    val outVis = Output(Bool())
  })

  def div(visible: Bool, dividend: UInt, divisor: UInt) = {
    val dividendReg = Reg(Vec(7, UInt(8.W)))
    val divisorReg  = Reg(Vec(7, UInt(barLen.W)))
    val remReg = Reg(Vec(8, UInt((barLen + 8).W)))
    val quoReg = Reg(Vec(8, UInt(8.W)))
    for (i <- 7 to 0 by -1) {
      if (i != 0) {
        if (i == 7) {
          dividendReg(6) := dividend
          divisorReg(6)  := divisor
        } else {
          dividendReg(i - 1) := dividendReg(i)
          divisorReg(i - 1)  := divisorReg(i)
        }
      }
      val rem = (if (i == 7) dividend(barLen + 7, 7) else ((remReg(i + 1) << 1.U) | dividendReg(i)(i)))
      val diff = rem.zext - (if (i == 7) divisor else divisorReg(i)).zext
      when (diff >= 0.S) {
        quoReg(i) := (if (i == 7) 1.U else (quoReg(i + 1)(6, 0) ## 1.U))
        remReg(i) := diff.asUInt
      } .otherwise {
        quoReg(i) := (if (i == 7) 0.U else (quoReg(i + 1)(6, 0) ## 0.U))
        remReg(i) := rem
      }
    }
    Mux(visible, quoReg(0), "h00".U)
  }

  val visReg = Reg(Vec(8, Bool()))
  visReg(0) := io.inVis
  for (i <- 1 until 8) {
    visReg(i) := visReg(i - 1)
  }
  io.outVis := visReg(7)

  val r = div(visReg(7), io.u * 255.U, io.a)
  val g = div(visReg(7), io.v * 255.U, io.a)
  val b = div(visReg(7), io.w * 255.U, io.a)
  io.pix := "hff".U ## b ## g ## r
}
