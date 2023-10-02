// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object FbReader {
  val nrBanks = Vram.dataWidth / IntRGB.alignedWidth
}

class FbReader extends Module {
  val io = IO(new Bundle {
      val fbIdx  = Input(UInt(FbSwapper.fbIdxWidth.W))
      val rdPos  = Input(TimingPos())
      val vram   = new RdAxi(Vram.addrWidth, Vram.dataWidth)
      val we     = Output(Bool())
      val wrIdx  = Output(UInt(log2Up(VgaTiming.width / FbReader.nrBanks).W))
      val wrLine = Output(UInt(log2Up(VgaTiming.height).W))
      val wrPix  = Output(Vec(FbReader.nrBanks, IntRGB()))
  })

  val wrLine  = RegInit(0.U(log2Up(VgaTiming.height).W))
  val valid   = RegInit(true.B)
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := ((io.fbIdx << log2Up(VgaTiming.width * VgaTiming.height)) |
                              (wrLine << log2Up(VgaTiming.width))) <<
                             log2Up(IntRGB.alignedWidth / 8)
  io.vram.addr.bits.len   := (VgaTiming.width / FbReader.nrBanks - 1).U
  io.vram.addr.bits.size  := Axi.size(Vram.dataWidth / 8)
  io.vram.addr.bits.burst := Axi.Burst.incr
  io.vram.addr.valid      := valid
  when (valid && io.vram.addr.ready) {
    valid := false.B
  }

  val wrIdx = RegInit(0.U(log2Up(VgaTiming.width / FbReader.nrBanks).W))
  io.vram.data.bits.id := DontCare
  io.vram.data.ready   := true.B
  io.we    := false.B
  io.wrPix := VecInit(Seq.tabulate(FbReader.nrBanks)(
    i => IntRGB.decode(io.vram.data.bits.data(
      IntRGB.width + i * IntRGB.alignedWidth - 1,
      i * IntRGB.alignedWidth
    ))
  ))
  when (io.vram.data.valid) {
    io.we := true.B
    wrIdx := wrIdx + 1.U
    when (wrIdx === (VgaTiming.width / FbReader.nrBanks - 1).U) {
      wrIdx := 0.U
    }
  }
  io.wrIdx := wrIdx

  when (io.rdPos.y < VgaTiming.height.U && io.rdPos.x === (VgaTiming.width - 1).U) {
    valid  := true.B
    wrLine := wrLine + 1.U
    when (wrLine === (VgaTiming.height - 1).U) {
      wrLine := 0.U
    }
  }
  io.wrLine := wrLine
}
