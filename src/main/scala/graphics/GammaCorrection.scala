// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import scala.math._

object gammaCorrect {
  val gamma = 2.2

  def apply(x: UInt) = {
    val res = WireDefault(0.U(x.getWidth.W))
    for (i <- 1 until pow(2, x.getWidth).toInt) {
      when (x === i.U) {
        val max = pow(2, x.getWidth) - 1
        res := (pow(i / max, gamma) * max).toInt.U
      }
    }
    res
  }
}
