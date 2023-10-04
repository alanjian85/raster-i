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

  val color = Wire(FbRGB())
  color.r := "hff".U
  color.g := "hff".U
  color.b := "hff".U

  val idx  = RegInit(0.U(log2Up(VgaTiming.width / Fb.nrBanks).W))
  val line = RegInit(0.U(unsignedBitLength(VgaTiming.height).W))
  io.done := line === VgaTiming.height.U
  when (io.fbId =/= RegNext(io.fbId)) {
    line := 0.U
  }

  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req.valid     := line =/= VgaTiming.height.U
  fbWriter.io.req.bits.line := line
  fbWriter.io.pix := VecInit(Seq.fill(4)(color))
  when (line =/= VgaTiming.height.U && fbWriter.io.req.ready) {
    idx := idx + 1.U
    when (idx === (VgaTiming.width / Fb.nrBanks - 1).U) {
      idx  := 0.U
      line := line + 1.U
    }
  }
}
