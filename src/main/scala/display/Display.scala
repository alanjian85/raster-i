// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val vga  = new VgaExt
  })

  val vgaSignal = Module(new VgaSignal)
  val vgaPos    = RegNext(vgaSignal.io.nextPos)
  vgaSignal.io.currPos := vgaPos
  io.vga := vgaSignal.io.vga

  val rdReqValid = RegInit(true.B)
  val rdReqLine  = RegInit(0.U(log2Up(VgaTiming.height).W))
  when (vgaPos.y < VgaTiming.height.U && vgaPos.x === (VgaTiming.width - 1).U) {
    rdReqValid := true.B
    rdReqLine  := rdReqLine + 1.U
    when (rdReqLine === (VgaTiming.height - 1).U) {
      rdReqLine := 0.U
    }
  }

  val buffer   = SyncReadMem(VgaTiming.width / Fb.nrBanks, Vec(Fb.nrBanks, VgaRGB()))
  val fbReader = Module(new FbReader)
  io.vram <> fbReader.io.vram
  fbReader.io.fbId := io.fbId
  fbReader.io.req.valid     := rdReqValid
  fbReader.io.req.bits.line := rdReqLine
  when (rdReqValid && fbReader.io.req.ready) {
    rdReqValid := false.B
  }
  val ditherer = Module(new Ditherer)
  ditherer.io.in  := fbReader.io.res
  ditherer.io.row := rdReqLine
  when (ditherer.io.out.valid) {
    buffer.write(ditherer.io.out.bits.idx, ditherer.io.out.bits.pix)
  }

  val pixBanks = buffer.read(vgaSignal.io.nextPos.x >> log2Up(Fb.nrBanks))
  vgaSignal.io.pix := pixBanks(vgaPos.x(log2Up(Fb.nrBanks) - 1, 0))
}
