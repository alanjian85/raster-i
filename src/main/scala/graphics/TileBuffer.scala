// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object Tile {
  val size = 16

  val nrCols = (Fb.width  + size - 1) / size
  val nrRows = (Fb.height + size - 1) / size

  val width  = nrCols * size
  val height = nrRows * size

  def apply() = new Tile
}

class Tile extends Bundle {
  val elem = Vec(Tile.size, Vec(Tile.size, FbRGB()))

  def apply(idx: Int)  = elem(idx)
  def apply(idx: UInt) = elem(idx)
}

class TileBuffer extends Module {
  val io = IO(new Bundle {
    val inReq  = Flipped(Decoupled(new Tile))
    val outReq = Irrevocable(new FbWrReq)
  })

  val buf = SyncReadMem(2 * Tile.nrCols, Tile())
  val fron = RegInit(0.U(log2Up(buf.length).W))
  val rear = RegInit(0.U(log2Up(buf.length).W))
  val size = RegInit(0.U(unsignedBitLength(buf.length).W))

  val nextRear = Mux(rear =/= (buf.length - 1).U, rear + 1.U, 0.U)
  val full     = nextRear === fron
  io.inReq.ready := !full
  when (!full && io.inReq.valid) {
    buf.write(rear, io.inReq.bits)
    rear := nextRear
    size := size + 1.U
  }

  val col  = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row  = RegInit(0.U(log2Up(Tile.size).W))
  val idx  = RegInit(0.U(log2Up(Tile.size / Fb.nrBanks).W))
  val nextFron = WireDefault(fron)
  val nextCol  = WireDefault(col)
  val nextRow  = WireDefault(row)
  val nextIdx  = WireDefault(idx)
  val writing  = size >= Tile.nrCols.U
  io.outReq.valid := writing
  val pix = Wire(Vec(Fb.nrBanks, FbRGB()))
  for (i <- 0 until Fb.nrBanks) {
    pix(i) := buf.read(nextFron + nextCol)(nextRow)(nextIdx << log2Up(Fb.nrBanks) | i.U)
  }
  io.outReq.bits.pix := pix
  when (writing && io.outReq.ready) {
    nextIdx := idx + 1.U
    idx     := nextIdx
    when (idx === (Tile.size / Fb.nrBanks - 1).U) {
      nextIdx := 0.U
      nextCol := col + 1.U
      col := nextCol
      when (col === (Tile.nrCols - 1).U) {
        nextCol := 0.U
        nextRow := row + 1.U
        row     := nextRow
        when (row === (Tile.size - 1).U) {
          row      := 0.U
          nextFron := fron + Tile.nrCols.U
          fron     := nextFron
          when (fron === (buf.length - Tile.nrCols).U) {
            nextFron := 0.U
          }
          size := size - Tile.nrCols.U
        }
      }
    }
  }
}
