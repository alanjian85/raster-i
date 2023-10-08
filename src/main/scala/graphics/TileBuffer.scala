// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object Tile {
  val width = 32

  val row = (Fb.width  + width - 1) / width * width
  val col = (Fb.height + width - 1) / width * width
}

class Tile extends Bundle {
  val elem = Vec(Tile.width, Vec(Tile.width, FbRGB()))

  def apply(idx: Int)  = elem(idx)
  def apply(idx: UInt) = elem(idx)
}

class TileBuffer extends Module {
  val io = IO(new Bundle {
    val inReq  = Flipped(Decoupled(new Tile))
    val outReq = Irrevocable(new FbWrReq)
  })


}
