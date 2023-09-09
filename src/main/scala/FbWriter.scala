// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class FbWrReq extends Bundle {
    val x = UInt(log2Up(Screen.width).W)
    val y = UInt(log2Up(Screen.height).W)
    val pixVec = Vec(4, UInt(32.W))
}

class FbWriter extends Module {
    val io = IO(new Bundle {
        val req = Flipped(Decoupled(new FbWrReq))
        val axi = new WrAxi(28, 128)
    })

    val addrReg = RegInit(0.U(28.W))
    val addrEmptyReg = RegInit(true.B)
    io.axi.addr.bits.id    := DontCare
    io.axi.addr.bits.addr  := addrReg
    io.axi.addr.bits.len   := 0.U
    io.axi.addr.bits.size  := "b100".U
    io.axi.addr.bits.burst := "b01".U
    io.axi.addr.valid := !addrEmptyReg
    when (io.axi.addr.valid && io.axi.addr.ready) {
        addrEmptyReg := true.B
    }

    val dataReg = RegInit(0.U(128.W))
    val dataEmptyReg = RegInit(true.B)
    io.axi.data.bits.data := dataReg
    io.axi.data.bits.strb := "hffff".U
    io.axi.data.bits.last := true.B
    io.axi.data.valid := !dataEmptyReg
    when (io.axi.data.valid && io.axi.data.ready) {
        dataEmptyReg := true.B
    }

    io.axi.resp.ready := true.B
    io.req.ready := addrEmptyReg && dataEmptyReg

    when (io.req.valid && io.req.ready) {
        addrReg := (io.req.bits.y * Screen.width.U + io.req.bits.x) << 2.U
        addrEmptyReg := false.B
        dataReg := io.req.bits.pixVec.reverse.reduce(_ ## _)
        dataEmptyReg := false.B
    }
}