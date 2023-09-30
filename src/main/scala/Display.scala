// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Display extends Module {
    val io = IO(new Bundle {
        val fbIdx = Input(UInt(1.W))
        val axi = new RdAxi(28, 128)
        val vga = new VgaExt
    })

    val scanSize = VgaTiming.width >> 2
    val scanBuffer = SyncReadMem(scanSize, Vec(4, UInt(12.W)))

    val vgaSignal = Module(new VgaSignal)
    val vgaData = scanBuffer.read(vgaSignal.io.nextPos.x >> 2.U)
    val vgaPos = RegNext(vgaSignal.io.nextPos)
    vgaSignal.io.currPos := vgaPos
    val idx = vgaPos.x & "b11".U
    vgaSignal.io.pix.r := vgaData(idx)(3, 0)
    vgaSignal.io.pix.g := vgaData(idx)(7, 4)
    vgaSignal.io.pix.b := vgaData(idx)(11, 8)
    io.vga := vgaSignal.io.vga

    io.axi.addr.bits.id := DontCare
    val lineIdxReg = RegInit(0.U(log2Up(VgaTiming.height).W))
    val rdValidReg = RegInit(true.B)
    when (vgaSignal.io.nextPos.x === (VgaTiming.width - 1).U &&
          vgaSignal.io.nextPos.y < VgaTiming.height.U) {
        lineIdxReg := lineIdxReg + 1.U
        when (lineIdxReg === (VgaTiming.height - 1).U) {
            lineIdxReg := 0.U
        }
        rdValidReg := true.B
    }
    when (rdValidReg && io.axi.addr.ready) {
        rdValidReg := false.B
    }
    io.axi.addr.bits.addr := (io.fbIdx << 22.U) | (lineIdxReg * scanSize.U << 4.U)
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.bits.len := (scanSize - 1).U
    io.axi.addr.bits.size := "b100".U
    io.axi.addr.valid := rdValidReg

    val ditherer = Module(new Ditherer)
    ditherer.io.row := vgaSignal.io.nextPos.y(1, 0)
    val rdData = io.axi.data.bits.data
    ditherer.io.inPix := VecInit(
      rdData( 23,  16) ## rdData( 15,   8) ## rdData(  7,  0),
      rdData( 55,  48) ## rdData( 47,  40) ## rdData( 39, 32),
      rdData( 87,  80) ## rdData( 79,  72) ## rdData( 71, 64),
      rdData(119, 112) ## rdData(111, 104) ## rdData(103, 96)
    )

    io.axi.data.bits.id := DontCare
    val recvIdxReg = RegInit(0.U(log2Up(scanSize).W))
    io.axi.data.ready := true.B
    when (io.axi.data.valid) {
        recvIdxReg := recvIdxReg + 1.U
        when (recvIdxReg === (scanSize - 1).U) {
            recvIdxReg := 0.U
        }
        scanBuffer.write(recvIdxReg, ditherer.io.outPix)
    }
}
