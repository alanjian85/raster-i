// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

object Main extends App {
  val emitArgs = Array("--target-dir", "generated")
  //emitVerilog(new TrinitySdl, emitArgs)
  emitVerilog(new Top, emitArgs)
}
