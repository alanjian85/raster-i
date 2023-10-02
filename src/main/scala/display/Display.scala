// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
  val io = IO(new Bundle {
    val fbIdx = Input(UInt(FbSwapper.fbIdxWidth.W))
    val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val vga   = new VgaExt
  })

  val vgaSignal = Module(new VgaSignal)
  val vgaPos    = RegNext(vgaSignal.io.nextPos)
  vgaSignal.io.currPos := vgaPos
  io.vga               := vgaSignal.io.vga

  val buffer   = SyncReadMem(VgaTiming.width / FbReader.nrBanks, Vec(FbReader.nrBanks, ExtRGB()))
  val fbReader = Module(new FbReader)
  fbReader.io.fbIdx := io.fbIdx
  fbReader.io.rdPos := vgaSignal.io.nextPos
  io.vram <> fbReader.io.vram
  when (fbReader.io.we) {
    val ditherer = Module(new Ditherer)
    ditherer.io.in  := fbReader.io.wrPix
    ditherer.io.row := fbReader.io.wrLine
    buffer.write(fbReader.io.wrIdx, ditherer.io.out)
  }

  val pixBanks = buffer.read(vgaSignal.io.nextPos.x >> log2Up(FbReader.nrBanks))
  vgaSignal.io.pix := pixBanks(vgaPos.x(log2Up(FbReader.nrBanks) - 1, 0))
}
