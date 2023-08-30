// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object Screen {
    val width = 800
    val height = 600

    val hfrontEnd = width + 40
    val hsyncEnd = hfrontEnd + 128
    val hbackEnd = hsyncEnd + 88

    val vfrontEnd = height + 1
    val vsyncEnd = vfrontEnd + 4
    val vbackEnd = vsyncEnd + 23
}

object ScreenPos {
  def apply(x: Int = 0) = {
    UVec2Init(x.U(log2Up(Screen.hbackEnd).W), x.U(log2Up(Screen.vbackEnd).W))
  }

  def apply(x: Int, y: Int) = {
    UVec2Init(x.U(log2Up(Screen.hbackEnd).W), y.U(log2Up(Screen.vbackEnd).W))
  }
}