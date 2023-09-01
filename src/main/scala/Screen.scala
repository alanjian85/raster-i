// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object Screen {
    val width = 1024
    val height = 768

    val hfrontEnd = width + 24
    val hsyncEnd = hfrontEnd + 136
    val hbackEnd = hsyncEnd + 160

    val vfrontEnd = height + 3
    val vsyncEnd = vfrontEnd + 6
    val vbackEnd = vsyncEnd + 29

    val polarity = false
}

object ScreenPos {
  def apply(x: Int = 0) = {
    UVec2Init(x.U(log2Up(Screen.hbackEnd).W), x.U(log2Up(Screen.vbackEnd).W))
  }

  def apply(x: Int, y: Int) = {
    UVec2Init(x.U(log2Up(Screen.hbackEnd).W), y.U(log2Up(Screen.vbackEnd).W))
  }
}