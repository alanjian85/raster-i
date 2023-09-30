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
        val fbIdx = Input(UInt(1.W))
        val req = Flipped(Decoupled(new FbWrReq))
        val axi = new WrAxi(28, 128)
    })

    val cacheAvail = RegInit(VecInit(Seq.fill(2)(true.B)))
    val cacheY = RegInit(VecInit(Seq.fill(2)(0.U(log2Up(VgaTiming.height).W))))
    val pixCache = Mem(256 * 2, Vec(4, UInt(32.W)))
    // val visCache = Mem(256 * 2, Vec(4, Bool()))

    val ldCacheIdx = RegInit(0.U(1.W))
    val currX = RegInit(0.U(log2Up(VgaTiming.width / 4).W))
    val currY = RegInit(0.U(log2Up(VgaTiming.height).W))

    io.req.ready := cacheAvail(ldCacheIdx)

    when (!cacheAvail(ldCacheIdx)) {
      ldCacheIdx := ldCacheIdx + 1.U
    }

    when (cacheAvail(ldCacheIdx) && io.req.valid) {
        pixCache.write(ldCacheIdx * 256.U | currX, io.req.bits.pix)
        // visCache(ldCacheIdx).write(currX, io.req.bits.vis)
        currX := currX + 1.U
        when (currX === ((VgaTiming.width / 4) - 1).U) {
            cacheY(ldCacheIdx) := currY
            cacheAvail(ldCacheIdx) := false.B
            ldCacheIdx := ldCacheIdx + 1.U

            currX := 0.U
            currY := currY + 1.U
            when (currY === (VgaTiming.height - 1).U) {
              currY := 0.U
            }
        }
    }

    val wrCacheIdx = RegInit(0.U(1.W))
    io.axi.addr.bits.id    := DontCare
    io.axi.addr.bits.addr  := (io.fbIdx << 22.U) | 
                              (VgaTiming.width.U * cacheY(wrCacheIdx) << 2.U)
    io.axi.addr.bits.len   := 255.U
    io.axi.addr.bits.size  := "b100".U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.valid      := false.B 

    val wrCurrIdx  = RegInit(0.U(8.W))
    io.axi.data.bits.data := pixCache
                               .read(wrCacheIdx * 256.U | wrCurrIdx)
                               .reverse
                               .reduce(_ ## _)
    io.axi.data.bits.strb := "hffff".U
    /* 
    visCache.read(wrCurrIdx)
      .map(_.asUInt)
      .reverse
      .reduce(Fill(4, _) ## Fill(4, _)) 
    */
    io.axi.data.bits.last := wrCurrIdx === 255.U
    io.axi.data.valid     := false.B

    io.axi.resp.ready := true.B

    object WrState extends ChiselEnum {
        val idle, wrAddr, wrData = Value 
    }
    import WrState._
    val wrStateReg = RegInit(idle)

    switch (wrStateReg) {
        is(idle) {
            when (!cacheAvail(wrCacheIdx)) {
                wrStateReg := wrAddr 
            } .otherwise {
                wrCacheIdx := wrCacheIdx + 1.U            
            }
        }
        is(wrAddr) {
            io.axi.addr.valid := true.B
            when (io.axi.addr.ready) {
                wrStateReg := wrData
            }
        }
        is(wrData) {
            io.axi.data.valid := true.B
            when (io.axi.data.ready) {
                wrCurrIdx := wrCurrIdx + 1.U
                when (wrCurrIdx === 255.U) {
                    cacheAvail(wrCacheIdx) := true.B
                    wrCurrIdx := 0.U
                    wrCacheIdx := wrCacheIdx + 1.U
                    when (cacheAvail(wrCacheIdx + 1.U)) {
                      wrStateReg := idle
                    } .otherwise {
                      wrStateReg := wrAddr
                    }
                }
            }
        }
    }
}
