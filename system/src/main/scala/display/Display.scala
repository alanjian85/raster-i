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

  val buf       = SyncReadMem(Fb.nrIndices, Vec(Fb.nrBanks, VgaRGB()))
  val fbReader  = Module(new FbReader)
  val rdReqLine = RegInit(0.U(log2Up(Fb.height).W))
  io.vram <> fbReader.io.vram
  fbReader.io.fbId := io.fbId
  fbReader.io.req  := false.B
  when (vgaPos.y < VgaTiming.height.U && vgaPos.x === (VgaTiming.width - 1).U) {
    fbReader.io.req := true.B
    rdReqLine := rdReqLine + 1.U
    when (rdReqLine === (Fb.height - 1).U) {
      rdReqLine := 0.U
    }
  }
  when (fbReader.io.res.valid) {
    val pix = Wire(Vec(Fb.nrBanks, VgaRGB()))
    for (i <- 0 until Fb.nrBanks) {
      pix(i).r := dither(VgaRGB.rWidth, fbReader.io.res.bits.pix(i).r, rdReqLine, i)
      pix(i).g := dither(VgaRGB.gWidth, fbReader.io.res.bits.pix(i).g, rdReqLine, i)
      pix(i).b := dither(VgaRGB.bWidth, fbReader.io.res.bits.pix(i).b, rdReqLine, i)
    }
    buf.write(fbReader.io.res.bits.idx, pix)
  }

  val pixBanks = buf.read(vgaSignal.io.nextPos.x >> log2Up(Fb.nrBanks))
  vgaSignal.io.pix := pixBanks(vgaPos.x(log2Up(Fb.nrBanks) - 1, 0))
}
