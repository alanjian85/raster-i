// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class Dither extends Module {
    val io = IO(new Bundle {
        val renderDone = Input(Bool())
        val axi = new RdWrAxi(28, 32)
    })

    object CmdState extends ChiselEnum {
        val start, skip, rdSending, rdWaiting, wrAddrSending, wrDataSending, done = Value
    }

    val cmdStateReg = RegInit(CmdState.start)

    val currXReg = RegInit(0.U(log2Up(Screen.width).W))
    val currYReg = RegInit(0.U(log2Up(Screen.height).W))
    val pixReg = RegInit(0.U(unsignedBitLength(4).W))

    val xReg = RegInit(0.U(log2Up(Screen.width).W))
    val yReg = RegInit(0.U(log2Up(Screen.height).W))
    val addr = yReg * Screen.width.U + xReg << 2.U
    val mul = WireDefault(0.U(unsignedBitLength(7).W))
    val skip = WireDefault(false.B)
    switch (pixReg) {
        is(0.U) {
            xReg := currXReg
            yReg := currYReg
            mul := 0.U
        }
        is(1.U) {
            xReg := currXReg + 1.U
            yReg := currYReg
            mul := 7.U
            when (currXReg === (Screen.width - 1).U) {
                skip := true.B
            }
        }
        is(2.U) {
            xReg := currXReg - 1.U
            yReg := currYReg + 1.U
            mul := 3.U
            when (currXReg === 0.U || currYReg === (Screen.height - 1).U) {
                skip := true.B
            }
        }
        is(3.U) {
            xReg := currXReg
            yReg := currYReg + 1.U
            mul := 5.U
            when (currYReg === (Screen.height - 1).U) {
                skip := true.B
            }
        }
        is(4.U) {
            xReg := currXReg + 1.U
            yReg := currYReg + 1.U
            mul := 1.U
            when (currXReg === (Screen.width - 1).U || currYReg === (Screen.height - 1).U) {
                skip := true.B
            }
        }
    }
    when (cmdStateReg === CmdState.start) {
        when (skip) {
            cmdStateReg := CmdState.skip
        } .otherwise {
            cmdStateReg := CmdState.rdSending
        }
    }

    io.axi.rdAddr.bits.id := DontCare
    io.axi.rdAddr.bits.addr := addr
    io.axi.rdAddr.bits.len := 0.U
    io.axi.rdAddr.bits.size := "b010".U
    io.axi.rdAddr.bits.burst := "b01".U
    io.axi.rdAddr.valid := (cmdStateReg === CmdState.rdSending) && io.renderDone
    when (io.axi.rdAddr.valid && io.axi.rdAddr.ready) {
        cmdStateReg := CmdState.rdWaiting
    }

    val errorReg = RegInit(0.U(32.W))
    val wrDataReg = RegInit(0.U(32.W))
    io.axi.rdData.ready := true.B
    when (cmdStateReg === CmdState.rdWaiting && io.axi.rdData.valid) {
        val rdData = io.axi.rdData.bits.data
        when (pixReg === 0.U) {
            errorReg := (0.U(4.W) ## rdData(27, 24)) ##
                        (0.U(4.W) ## rdData(19, 16)) ##
                        (0.U(4.W) ## rdData(11,  8)) ##
                        (0.U(4.W) ## rdData( 3,  0))
            cmdStateReg := CmdState.skip
        } .otherwise {
            wrDataReg := rdData(31, 24) + (errorReg(31, 24) * mul >> 4.U) ##
                         rdData(23, 16) + (errorReg(23, 16) * mul >> 4.U) ##
                         rdData(15, 8)  + (errorReg(15,  8) * mul >> 4.U) ##
                         rdData(7, 0)   + (errorReg( 7,  0) * mul >> 4.U)
            cmdStateReg := CmdState.wrAddrSending
        }
    }

    io.axi.wrAddr.bits.id := DontCare
    io.axi.wrAddr.bits.addr := addr
    io.axi.wrAddr.bits.len := 0.U
    io.axi.wrAddr.bits.size := "b010".U
    io.axi.wrAddr.bits.burst := "b01".U
    io.axi.wrAddr.valid := cmdStateReg === CmdState.wrAddrSending
    when (io.axi.wrAddr.valid && io.axi.wrAddr.ready) {
        cmdStateReg := CmdState.wrDataSending
    }
    
    io.axi.wrData.bits.data := wrDataReg
    io.axi.wrData.bits.strb := "hf".U
    io.axi.wrData.bits.last := true.B
    io.axi.wrData.valid := cmdStateReg === CmdState.wrDataSending
    when ((cmdStateReg === CmdState.skip) || (io.axi.wrData.valid && io.axi.wrData.ready)) {
        pixReg := pixReg + 1.U
        cmdStateReg := CmdState.start
        when (pixReg === 4.U) {
            pixReg := 0.U
            currXReg := currXReg + 1.U
            when (currXReg === (Screen.width - 1).U) {
                currXReg := 0.U
                currYReg := currYReg + 1.U
                when (currYReg === (Screen.height - 1).U) {
                    cmdStateReg := CmdState.done
                }
            }
        }
    }

    io.axi.wrResp.ready := true.B
}