// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbReader(convert: (Vec[RGB], UInt) => Vec[RGB]) extends Module {
  val nrBanks = Vram.dataWidth / IntRGB.alignedWidth

  val io = IO(new Bundle {
      val fbIdx = Input(UInt(1.W))
      val pos   = Input(TimingPos())
      val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
      val pix   = Output(Vec(nrBanks, ExtRGB()))
  })

  val buffer  = SyncReadMem(VgaTiming.width / nrBanks, Vec(nrBanks, ExtRGB()))

  val addrWidth = log2Up(VgaTiming.width * VgaTiming.height)
  val scanline  = RegInit(0.U(log2Up(VgaTiming.height).W))
  val valid     = RegInit(true.B)
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := ((io.fbIdx << addrWidth) |
                              (scanline * VgaTiming.width.U)) <<
                             log2Up(IntRGB.alignedWidth / 8)
  io.vram.addr.bits.len   := (VgaTiming.width / nrBanks - 1).U
  io.vram.addr.bits.size  := Axi.size(Vram.dataWidth / 8)
  io.vram.addr.bits.burst := Axi.Burst.incr
  io.vram.addr.valid      := valid
  when (valid && io.vram.addr.ready) {
    valid := false.B
  }

  val rdIdx = RegInit(0.U(log2Up(VgaTiming.width / nrBanks).W))
  io.vram.data.bits.id := DontCare
  io.vram.data.ready   := true.B
  when (io.vram.data.valid) {
    buffer.write(rdIdx, convert(VecInit(Seq.tabulate(nrBanks)(
      i => IntRGB.decode(io.vram.data.bits.data(
        IntRGB.width + i * IntRGB.alignedWidth - 1,
        i * IntRGB.alignedWidth
      ))
    )), scanline))
    rdIdx := rdIdx + 1.U
    when (rdIdx === (VgaTiming.width / nrBanks - 1).U) {
      rdIdx := 0.U
    }
  }

  io.pix := buffer.read(io.pos.x / nrBanks.U)
  when (io.pos.y < VgaTiming.height.U && io.pos.x === (VgaTiming.width - 1).U) {
    scanline := scanline + 1.U
    when (scanline === (VgaTiming.height - 1).U) {
      scanline := 0.U
    }
    valid := true.B
  }
}
