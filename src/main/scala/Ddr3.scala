// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.experimental._

class Ddr3 extends Bundle {
    val addr           = Output(UInt(14.W))
    val ba             = Output(UInt(3.W))
    val cas_n          = Output(Bool())
    val ck_n           = Output(UInt(1.W))
    val ck_p           = Output(UInt(1.W))
    val cke            = Output(UInt(1.W))
    val ras_n          = Output(Bool())
    val reset_n        = Output(Bool())
    val we_n           = Output(Bool())
    val dq             = Analog(16.W)
    val dqs_n          = Analog(2.W)
    val dqs_p          = Analog(2.W)
    val cs_n           = Output(UInt(1.W))
    val dm             = Output(UInt(2.W))
    val odt            = Output(UInt(1.W)) 
}