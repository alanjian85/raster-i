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
  val color = Wire(FbRGB())
  color.r := RegNext(fbWriter.io.idx)
  color.g := color.r
  color.b := color.r
  fbWriter.io.pix := VecInit(Seq.fill(4)(color))
  when (line =/= VgaTiming.height.U && fbWriter.io.req.ready) {
    line := line + 1.U
  }
}
