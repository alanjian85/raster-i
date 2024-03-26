// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object dither {
  def apply(dstWidth: Int, x: UInt, row: UInt, col: Int) = {
    val width = x.getWidth - dstWidth
    val res = Wire(UInt(dstWidth.W))
    if (width > 0) {
      val bayer = Wire(Vec(width, Vec(width, UInt(width.W))))
      for (i <- 0 until width) {
        for (j <- 0 until width) {
          var a = i ^ j
          var b = i
          var c = 0
          for (k <- 0 until log2Up(width)) {
            c <<= 2
            c |= (a & 1) << 1
            c |= b & 1
            a >>= 1
            b >>= 1
          }
          bayer(i)(j) := c.U
        }
      }
      val sum = x +& bayer(row)(col)
      res := sum >> dstWidth
      when (sum(x.getWidth)) {
          res := Fill(dstWidth, 1.U)
      }
    } else {
      res := x ## 0.U((-width).W)
    }
    res
  }
}
