// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

object FbSwapper {
  val fbIdxWidth = 1
}

class FbSwapper extends Module {
  val io = IO(new Bundle {
    val displayVsync  = Input(Bool())
    val graphicsDone  = Input(Bool())
    val displayFbIdx  = Output(UInt(FbSwapper.fbIdxWidth.W))
    val graphicsFbIdx = Output(UInt(FbSwapper.fbIdxWidth.W))
  })

  val displayFbIdx  = RegInit(0.U(FbSwapper.fbIdxWidth.W))
  val graphicsFbIdx = RegInit(1.U(FbSwapper.fbIdxWidth.W))
  io.displayFbIdx  := displayFbIdx
  io.graphicsFbIdx := graphicsFbIdx

  val swapped = RegInit(false.B)
  when (!swapped && io.displayVsync && io.graphicsDone) {
    displayFbIdx  := ~displayFbIdx
    graphicsFbIdx := ~graphicsFbIdx
    swapped       := true.B
  }
  when (!io.graphicsDone) {
    swapped := false.B
  }
}
