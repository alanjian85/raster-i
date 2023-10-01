// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object VgaTiming {
  val width  = 1024
  val height = 768

  val hfron = 24
  val hsync = 136
  val hback = 160
  val hsyncSta = width    + hfron
  val hsyncEnd = hsyncSta + hsync
  val horizEnd = hsyncEnd + hback

  val vfron = 3
  val vsync = 6
  val vback = 29
  val vsyncSta = height   + vfron
  val vsyncEnd = vsyncSta + vsync
  val vertiEnd = vsyncEnd + vback

  val polarity = false
}

object TimingPos extends UVec2Factory(log2Up(VgaTiming.horizEnd), log2Up(VgaTiming.vertiEnd))
object ScreenPos extends UVec2Factory(log2Up(VgaTiming.width), log2Up(VgaTiming.height))

class VgaExt extends Bundle {
  val r     = Output(UInt(ExtRGB.rWidth.W))
  val g     = Output(UInt(ExtRGB.gWidth.W))
  val b     = Output(UInt(ExtRGB.bWidth.W))
  val hsync = Output(Bool())
  val vsync = Output(Bool())
}

class VgaSignal extends Module {
  val io = IO(new Bundle {
    val pix     = Input(ExtRGB())
    val currPos = Input(TimingPos())
    val nextPos = Output(TimingPos())
    val vga     = new VgaExt
  })

  val nextPos = RegInit(TimingPos(0, VgaTiming.height))
  nextPos.x := nextPos.x + 1.U
  when (nextPos.x === (VgaTiming.horizEnd - 1).U) {
    nextPos.x := 0.U
    nextPos.y := nextPos.y + 1.U
    when (nextPos.y === (VgaTiming.vertiEnd - 1).U) {
      nextPos.y := 0.U
    }
  }
  io.nextPos := nextPos

  val active = io.currPos.x < VgaTiming.width.U && io.currPos.y < VgaTiming.height.U
  io.vga.r := Mux(active, io.pix.r, 0.U)
  io.vga.g := Mux(active, io.pix.g, 0.U)
  io.vga.b := Mux(active, io.pix.b, 0.U)

  val hsync = VgaTiming.hsyncSta.U <= io.currPos.x && io.currPos.x < VgaTiming.hsyncEnd.U
  val vsync = VgaTiming.vsyncSta.U <= io.currPos.y && io.currPos.y < VgaTiming.vsyncEnd.U
  io.vga.hsync := hsync === VgaTiming.polarity.B
  io.vga.vsync := vsync === VgaTiming.polarity.B
}