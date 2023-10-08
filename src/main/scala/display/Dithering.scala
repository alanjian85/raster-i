// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

object dither {
  def apply(x: UInt, row: UInt, col: Int) = {
    object BayerMat4 extends UMat4Factory(4)
    val bayer = BayerMat4(
       0,  8,  2, 10,
      12,  4, 14,  6,
       3, 11,  1,  9,
      15,  7, 13,  5
    )

    val sum = x +& bayer(row)(col)
    val res = WireDefault(sum(7, 4))
    when (sum(8)) {
        res := "hf".U
    }
    res
  }
}
