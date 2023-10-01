// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbReader(preprocess: (UInt, Vec[RGB]) => Vec[RGB]) extends Module {
  val io = IO(new Bundle {
      val fbIdx = Input(UInt(1.W))
      val pos   = Input(TimingPos())
      val vram  = new RdAxi(28, 128)
      val pix   = Output(Vec(4, RGB444()))
  })

  val buffer = SyncReadMem(VgaTiming.width / 4, Vec(4, RGB444()))

  val addrWidth = log2Up(VgaTiming.width * VgaTiming.height) + 2
  val scanline  = RegInit(0.U(log2Up(VgaTiming.height).W))
  val valid     = RegInit(true.B)
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := (io.fbIdx << addrWidth) | (scanline * VgaTiming.width.U << 2)
  io.vram.addr.bits.len   := (VgaTiming.width / 4 - 1).U
  io.vram.addr.bits.size  := "b100".U
  io.vram.addr.bits.burst := "b01".U
  io.vram.addr.valid      := valid
  when (valid && io.vram.addr.ready) {
    valid := false.B
  }

  val rdIdx = RegInit(0.U(log2Up(VgaTiming.width / 4).W))
  io.vram.data.bits.id := DontCare
  io.vram.data.ready   := true.B
  when (io.vram.data.valid) {
    buffer.write(rdIdx, preprocess(scanline, VecInit(
      RGB888.decode(io.vram.data.bits.data( 23,  0)),
      RGB888.decode(io.vram.data.bits.data( 55, 32)),
      RGB888.decode(io.vram.data.bits.data( 87, 64)),
      RGB888.decode(io.vram.data.bits.data(119, 96))
    )))
    rdIdx := rdIdx + 1.U
    when (rdIdx === (VgaTiming.width / 4 - 1).U) {
      rdIdx := 0.U
    }
  }

  io.pix := buffer.read(io.pos.x >> 2)
  when (io.pos.y < VgaTiming.height.U && io.pos.x === (VgaTiming.width - 1).U) {
    scanline := scanline + 1.U
    when (scanline === (VgaTiming.height - 1).U) {
      scanline := 0.U
    }
    valid := true.B
  }
}
