// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbWrReq extends Bundle {
  val pix = UInt(32.W)
  val vis = Bool()
}

class FbWriter extends Module {
    val io = IO(new Bundle {
        val req = Flipped(Decoupled(new FbWrReq))
        val axi = new WrAxi(28, 128)
    })

    val pixCache = Seq.fill(4)(Mem(256, UInt(32.W)))
    val visCache = Seq.fill(4)(Mem(256, Bool()))
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
    io.axi.data.bits.data := pixCache(3).read(wrIdxReg) ##
                             pixCache(2).read(wrIdxReg) ##
                             pixCache(1).read(wrIdxReg) ##
                             pixCache(0).read(wrIdxReg)
    io.axi.data.bits.strb := Fill(4, visCache(3).read(wrIdxReg)) ##
                             Fill(4, visCache(2).read(wrIdxReg)) ##
                             Fill(4, visCache(1).read(wrIdxReg)) ##
                             Fill(4, visCache(0).read(wrIdxReg))
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
                        pixCache(0).write(xReg >> 2.U, io.req.bits.pix)
                        visCache(0).write(xReg >> 2.U, io.req.bits.vis)
                    }
                    is(1.U) {
                        pixCache(1).write(xReg >> 2.U, io.req.bits.pix)
                        visCache(1).write(xReg >> 2.U, io.req.bits.vis)
                    }
                    is(2.U) {
                        pixCache(2).write(xReg >> 2.U, io.req.bits.pix)
                        visCache(2).write(xReg >> 2.U, io.req.bits.vis)
                    }
                    is(3.U) {
                        pixCache(3).write(xReg >> 2.U, io.req.bits.pix)
                        visCache(3).write(xReg >> 2.U, io.req.bits.vis)
                    }
                }
                xReg := xReg + 1.U
                when (xReg === (Screen.width - 1).U) {
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
