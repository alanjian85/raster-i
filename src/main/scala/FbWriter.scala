// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbWriter extends Module {
    val io = IO(new Bundle {
        val req = Flipped(Decoupled(UInt(32.W)))
        val axi = new WrAxi(28, 128)
    })

    val cache = Seq.fill(4)(Mem(256, UInt(32.W)))
    val xReg  = RegInit(0.U(log2Up(Screen.width).W))
    val yReg  = RegInit(0.U(log2Up(Screen.height).W))

    io.req.ready := false.B

    io.axi.addr.bits.id    := DontCare
    io.axi.addr.bits.addr  := Screen.width.U * yReg << 2.U
    io.axi.addr.bits.len   := 255.U
    io.axi.addr.bits.size  := "b100".U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.valid      := false.B 

    val wrIdxReg  = RegInit(0.U(8.W))
    io.axi.data.bits.data := cache(3).read(wrIdxReg) ##
                             cache(2).read(wrIdxReg) ##
                             cache(1).read(wrIdxReg) ##
                             cache(0).read(wrIdxReg)
    io.axi.data.bits.strb := "hffff".U
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
                switch (xReg(1, 0)) {
                    is(0.U) {
                        cache(0).write(xReg >> 2.U, io.req.bits)
                    }
                    is(1.U) {
                        cache(1).write(xReg >> 2.U, io.req.bits)
                    }
                    is(2.U) {
                        cache(2).write(xReg >> 2.U, io.req.bits)
                    }
                    is(3.U) {
                        cache(3).write(xReg >> 2.U, io.req.bits)
                    }
                }
                xReg := xReg + 1.U
            }
            when (xReg === (Screen.width - 1).U) {
                xReg := 0.U
                stateReg := addrWriting
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
            }
            when (wrIdxReg === 255.U) {
                wrIdxReg := 0.U
                stateReg := avail
                yReg := yReg + 1.U
            }
            when (yReg === (Screen.height - 1).U) {
                yReg := 0.U
            }
        }
    }
}
