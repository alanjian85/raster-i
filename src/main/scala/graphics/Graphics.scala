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

  val line = RegInit(0.U(unsignedBitLength(Fb.height).W))
  val done = line === Fb.height.U
  val fbWriter = Module(new FbWriter)
  io.vram <> fbWriter.io.vram
  fbWriter.io.fbId := io.fbId
  fbWriter.io.req.valid := !done
  val color = Wire(FbRGB())
  color.r := RegNext(fbWriter.io.idx(7, 4) ## 0.U(4.W))
  color.g := color.r
  color.b := color.r
  fbWriter.io.req.bits.pix := VecInit(Seq.fill(Fb.nrBanks)(
    color.map(color => Mux(line < (Fb.height / 2).U, color, gammaCorrect(color)))
  ))
  when (!done && fbWriter.io.req.ready && fbWriter.io.idx === 0.U) {
    line := line + 1.U
  }
  when (RegNext(io.fbId) =/= io.fbId) {
    line := 0.U
  }
  io.done := done
}
