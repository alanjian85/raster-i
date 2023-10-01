// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Top extends Module {
  val io = IO(new TrinityIO)

  val trinity = Module(new Trinity)
  trinity.reset := !reset.asBool
  io <> trinity.io
}
