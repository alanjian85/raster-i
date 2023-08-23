// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.experimental._

class Ddr3PhyIO extends Bundle {
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

class SdramController extends BlackBox { 
    val io = IO(new Bundle {
        val ddr3 = new Ddr3PhyIO
        val init_calib_complete = Output(Bool())

        val app_addr          = Input(UInt(28.W))
        val app_cmd           = Input(UInt(3.W))
        val app_en            = Input(Bool())
        val app_wdf_data      = Input(UInt(128.W))
        val app_wdf_end       = Input(Bool())
        val app_wdf_wren      = Input(Bool())
        val app_rd_data       = Output(UInt(128.W))
        val app_rd_data_end   = Output(Bool())
        val app_rd_data_valid = Output(Bool())
        val app_rdy           = Output(Bool())
        val app_wdf_rdy       = Output(Bool())
        val app_sr_req        = Input(Bool())
        val app_ref_req       = Input(Bool())
        val app_zq_req        = Input(Bool())
        val app_sr_active     = Output(Bool())
        val app_ref_ack       = Output(Bool())
        val app_zq_ack        = Output(Bool())
        val ui_clk            = Output(Bool())
        val ui_clk_sync_rst   = Output(Bool())
        val app_wdf_mask      = Input(UInt(16.W))

        val sys_clk_i = Input(Clock())
        val clk_ref_i = Input(Clock())
        val sys_rst   = Input(Bool())
    })
}
