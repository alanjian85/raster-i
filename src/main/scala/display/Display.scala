// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val vga   = new VgaExt
  })

  val vgaSignal = Module(new VgaSignal)
  val vgaPos    = RegNext(vgaSignal.io.nextPos)
  vgaSignal.io.currPos := vgaPos
  io.vga               := vgaSignal.io.vga

  val buffer   = SyncReadMem(VgaTiming.width / Fb.nrBanks, Vec(Fb.nrBanks, VgaRGB()))
  val fbReader = Module(new FbReader)
  fbReader.io.fbId := io.fbId
  fbReader.io.pos  := vgaSignal.io.nextPos
  io.vram <> fbReader.io.vram
  when (fbReader.io.req.valid) {
    val ditherer = Module(new Ditherer)
    ditherer.io.in  := fbReader.io.req.bits.pix
    ditherer.io.row := fbReader.io.req.bits.line
    buffer.write(fbReader.io.req.bits.idx, ditherer.io.out)
  }

  val pixBanks = buffer.read(vgaSignal.io.nextPos.x >> log2Up(Fb.nrBanks))
  vgaSignal.io.pix := pixBanks(vgaPos.x(log2Up(Fb.nrBanks) - 1, 0))
}
