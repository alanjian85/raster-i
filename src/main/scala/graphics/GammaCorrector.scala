// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import scala.math._

object GammaCorrector {
  val gamma = 2.2
}

class GammaCorrector extends Module {
  val io = IO(new Bundle {
    val in  = Input(FbRGB())
    val out = Output(FbRGB())
  })

  def correct(x: UInt, xWidth: Int) = {
    val res = WireDefault(0.U(xWidth.W))
    for (i <- 1 until pow(2, xWidth).toInt) {
      when (x === i.U) {
        val max = pow(2, xWidth) - 1
        res := (pow(i / max, GammaCorrector.gamma) * max).toInt.U
      }
    }
    res
  }

  io.out.r := correct(io.in.r, FbRGB.rWidth)
  io.out.g := correct(io.in.g, FbRGB.gWidth)
  io.out.b := correct(io.in.b, FbRGB.bWidth)
}
