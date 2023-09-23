// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbWrReq extends Bundle {
  val pix = Vec(4, UInt(32.W))
  val vis = Vec(4, Bool())
}

class FbWriter extends Module {
    val io = IO(new Bundle {
        val req = Flipped(Decoupled(new FbWrReq))
        val axi = new WrAxi(28, 128)
    })

    val pixCache = Mem(256, Vec(4, UInt(32.W)))
    val visCache = Mem(256, Vec(4, Bool()))
    val xReg  = RegInit(0.U(log2Up(Screen.width / 4).W))
    val yReg  = RegInit(0.U(log2Up(Screen.height).W))

    io.req.ready := false.B

    io.axi.addr.bits.id    := DontCare
    io.axi.addr.bits.addr  := Screen.width.U * yReg << 2.U
    io.axi.addr.bits.len   := 255.U
    io.axi.addr.bits.size  := "b100".U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.valid      := false.B 

    val wrIdxReg  = RegInit(0.U(8.W))
    io.axi.data.bits.data := pixCache.read(wrIdxReg).reverse.reduce(_ ## _)
    io.axi.data.bits.strb := visCache.read(wrIdxReg)
                                     .map(_.asUInt)
                                     .reverse
                                     .reduce(Fill(4, _) ## Fill(4, _)) 
    io.axi.data.bits.last := wrIdxReg === 255.U
    io.axi.data.valid     := false.B

    io.axi.resp.ready := true.B

    object State extends ChiselEnum {
        val avail, addrWriting, dataWriting = Value 
    }
    import State._
    val stateReg = RegInit(avail)

    switch (stateReg) {
        is(avail) {
            io.req.ready := true.B
            when (io.req.valid) {
                pixCache.write(xReg, io.req.bits.pix)
                visCache.write(xReg, io.req.bits.vis)
                xReg := xReg + 1.U
                when (xReg === ((Screen.width / 4) - 1).U) {
                    xReg := 0.U
                    stateReg := addrWriting
                }
            }
        }
        is(addrWriting) {
            io.axi.addr.valid := true.B
            when (io.axi.addr.ready) {
                stateReg := dataWriting
            }
        }
        is(dataWriting) {
            io.axi.data.valid := true.B
            when (io.axi.data.ready) {
                wrIdxReg := wrIdxReg + 1.U
                when (wrIdxReg === 255.U) {
                    wrIdxReg := 0.U
                    stateReg := avail
                    yReg := yReg + 1.U
                    when (yReg === (Screen.height - 1).U) {
                        yReg := 0.U
                    }
                }
            }

        }
    }
}
