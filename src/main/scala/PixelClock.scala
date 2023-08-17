// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class PixelClock extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val clk_pix = Output(Clock())
  })
}
