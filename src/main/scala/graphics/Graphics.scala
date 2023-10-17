// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Graphics extends Module {
  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = new WrAxi(Vram.addrWidth, Vram.dataWidth)
    val done = Output(Bool())
  })

  val col = RegInit(0.U(log2Up(Tile.nrCols).W))
  val row = RegInit(0.U(unsignedBitLength(Tile.nrRows).W))
  when (RegNext(io.fbId) =/= io.fbId) {
    col := 0.U
    row := 0.U
  }

  val tileBuffer = Module(new TileBuffer)
  val valid = row < Tile.nrRows.U
  tileBuffer.io.inReq.valid := valid
  when (valid && tileBuffer.io.inReq.ready) {
    col := col + 1.U
    when (col === (Tile.nrCols - 1).U) {
      col := 0.U
      row := row + 1.U
    }
  }
  for (i <- 0 until Tile.size) {
    for (j <- 0 until Tile.size) {
      tileBuffer.io.inReq.bits(i)(j).r := col << 2.U
      tileBuffer.io.inReq.bits(i)(j).g := row << 2.U
      tileBuffer.io.inReq.bits(i)(j).b := "hff".U
    }
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req <> tileBuffer.io.outReq
  io.done := !valid && fbWriter.io.done
}
