// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

class AxiRdAddr(addrWidth: Int, idWidth: Int = 0) extends Bundle {
    val id     = UInt(idWidth.W)
    val addr   = UInt(addrWidth.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
}

class AxiRdData(dataWidth: Int, idWidth: Int = 0) extends Bundle {
    val id   = UInt(idWidth.W)
    val data = UInt(dataWidth.W)
    val resp = UInt(2.W)
    val last = Bool()
}

class AxiWrAddr(addrWidth: Int, idWidth: Int = 0) extends Bundle {
    val id     = UInt(idWidth.W)
    val addr   = UInt(addrWidth.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
}

class AxiWrData(dataWidth: Int) extends Bundle {
    val data = UInt(dataWidth.W)
    val strb = UInt((dataWidth / 8).W)
    val last = Bool()
}

class AxiWrResp(idWidth: Int = 0) extends Bundle {
    val id   = UInt(idWidth.W)
    val resp = UInt(2.W)
}

class RdAxi(addrWidth: Int, dataWidth: Int, idWidth: Int = 0) extends Bundle {
    val addr = Irrevocable(new AxiRdAddr(addrWidth, idWidth))
    val data = Flipped(Irrevocable(new AxiRdData(dataWidth, idWidth)))
}

class WrAxi(addrWidth: Int, dataWidth: Int, idWidth: Int = 0) extends Bundle {
    val addr = Irrevocable(new AxiWrAddr(addrWidth, idWidth))
    val data = Irrevocable(new AxiWrData(dataWidth))
    val resp = Flipped(Irrevocable(new AxiWrResp(idWidth)))
}

class RdWrAxi(addrWidth: Int, dataWidth: Int, rdIdWidth: Int = 0, wrIdWidth: Int = 0) extends Bundle {
    val rdAddr = Irrevocable(new AxiRdAddr(addrWidth, rdIdWidth))
    val rdData = Flipped(Irrevocable(new AxiRdData(dataWidth, rdIdWidth)))
    val wrAddr = Irrevocable(new AxiWrAddr(addrWidth, wrIdWidth))
    val wrData = Irrevocable(new AxiWrData(dataWidth))
    val wrResp = Flipped(Irrevocable(new AxiWrResp(wrIdWidth)))
}

class RdAxiExt(addrWidth: Int, dataWidth: Int, idWidth: Int = 0) extends Bundle {
    val arid     = Input(UInt(idWidth.W))
    val araddr   = Input(UInt(addrWidth.W))
    val arlen    = Input(UInt(8.W))
    val arsize   = Input(UInt(3.W))
    val arburst  = Input(UInt(2.W))
    val arlock   = Input(Bool())
    val arcache  = Input(UInt(4.W))
    val arprot   = Input(UInt(3.W))
    val arqos    = Input(UInt(4.W))
    val arregion = Input(UInt(4.W))
    val arvalid  = Input(Bool())
    val arready  = Output(Bool())

    val rid    = Output(UInt(idWidth.W))
    val rdata  = Output(UInt(dataWidth.W))
    val rresp  = Output(UInt(2.W))
    val rlast  = Output(Bool())
    val rvalid = Output(Bool())
    val rready = Input(Bool())

    def connect(that: RdAxi) = {
        arid     := that.addr.bits.id
        araddr   := that.addr.bits.addr
        arlen    := that.addr.bits.len
        arsize   := that.addr.bits.size
        arburst  := that.addr.bits.burst
        arlock   := false.B
        arcache  := "b0011".U
        arprot   := "b000".U
        arqos    := 0.U
        arregion := 0.U
        arvalid  := that.addr.valid
        that.addr.ready := arready

        that.data.bits.id   := rid
        that.data.bits.data := rdata
        that.data.bits.resp := rresp
        that.data.bits.last := rlast
        that.data.valid     := rvalid
        rready := that.data.ready
    }
}

class WrAxiExt(addrWidth: Int, dataWidth: Int, idWidth: Int = 0) extends Bundle {
    val awid     = Input(UInt(idWidth.W))
    val awaddr   = Input(UInt(addrWidth.W))
    val awlen    = Input(UInt(8.W))
    val awsize   = Input(UInt(3.W))
    val awburst  = Input(UInt(2.W))
    val awlock   = Input(Bool())
    val awcache  = Input(UInt(4.W))
    val awprot   = Input(UInt(3.W))
    val awqos    = Input(UInt(4.W))
    val awregion = Input(UInt(4.W))
    val awvalid  = Input(Bool())
    val awready  = Output(Bool())

    val wdata  = Input(UInt(dataWidth.W))
    val wstrb  = Input(UInt((dataWidth / 8).W))
    val wlast  = Input(Bool())
    val wvalid = Input(Bool())
    val wready = Output(Bool())

    val bid    = Output(UInt(idWidth.W))
    val bresp  = Output(UInt(2.W))
    val bvalid = Output(Bool())
    val bready = Input(Bool())

    def connect(that: WrAxi) = {
        awid     := that.addr.bits.id
        awaddr   := that.addr.bits.addr
        awlen    := that.addr.bits.len
        awsize   := that.addr.bits.size
        awburst  := that.addr.bits.burst
        awlock   := false.B
        awcache  := "b0011".U
        awprot   := "b000".U
        awqos    := 0.U
        awregion := 0.U
        awvalid  := that.addr.valid
        that.addr.ready := awready

        wdata  := that.data.bits.data
        wstrb  := that.data.bits.strb
        wlast  := that.data.bits.last
        wvalid := that.data.valid
        that.data.ready := wready
    
        that.resp.bits.id   := bid
        that.resp.bits.resp := bresp
        that.resp.valid     := bvalid
        bready := that.resp.ready
    }
}

class RdWrAxiExt(addrWidth: Int, dataWidth: Int, rdIdWidth: Int = 0, wrIdWidth: Int = 0) extends Bundle {
    val arid     = Input(UInt(rdIdWidth.W))
    val araddr   = Input(UInt(addrWidth.W))
    val arlen    = Input(UInt(8.W))
    val arsize   = Input(UInt(3.W))
    val arburst  = Input(UInt(2.W))
    val arlock   = Input(Bool())
    val arcache  = Input(UInt(4.W))
    val arprot   = Input(UInt(3.W))
    val arqos    = Input(UInt(4.W))
    val arregion = Input(UInt(4.W))
    val arvalid  = Input(Bool())
    val arready  = Output(Bool())

    val rid    = Output(UInt(rdIdWidth.W))
    val rdata  = Output(UInt(dataWidth.W))
    val rresp  = Output(UInt(2.W))
    val rlast  = Output(Bool())
    val rvalid = Output(Bool())
    val rready = Input(Bool())

    val awid     = Input(UInt(wrIdWidth.W))
    val awaddr   = Input(UInt(addrWidth.W))
    val awlen    = Input(UInt(8.W))
    val awsize   = Input(UInt(3.W))
    val awburst  = Input(UInt(2.W))
    val awlock   = Input(Bool())
    val awcache  = Input(UInt(4.W))
    val awprot   = Input(UInt(3.W))
    val awqos    = Input(UInt(4.W))
    val awregion = Input(UInt(4.W))
    val awvalid  = Input(Bool())
    val awready  = Output(Bool())

    val wdata  = Input(UInt(dataWidth.W))
    val wstrb  = Input(UInt((dataWidth / 8).W))
    val wlast  = Input(Bool())
    val wvalid = Input(Bool())
    val wready = Output(Bool())

    val bid    = Output(UInt(wrIdWidth.W))
    val bresp  = Output(UInt(2.W))
    val bvalid = Output(Bool())
    val bready = Input(Bool())

    def connect(that: RdWrAxi) = {
        arid     := that.rdAddr.bits.id
        araddr   := that.rdAddr.bits.addr
        arlen    := that.rdAddr.bits.len
        arsize   := that.rdAddr.bits.size
        arburst  := that.rdAddr.bits.burst
        arlock   := false.B
        arcache  := "b0011".U
        arprot   := "b000".U
        arqos    := 0.U
        arregion := 0.U
        arvalid  := that.rdAddr.valid
        that.rdAddr.ready := arready

        that.rdData.bits.id   := rid
        that.rdData.bits.data := rdata
        that.rdData.bits.resp := rresp
        that.rdData.bits.last := rlast
        that.rdData.valid     := rvalid
        rready := that.rdData.ready

        awid     := that.wrAddr.bits.id
        awaddr   := that.wrAddr.bits.addr
        awlen    := that.wrAddr.bits.len
        awsize   := that.wrAddr.bits.size
        awburst  := that.wrAddr.bits.burst
        awlock   := false.B
        awcache  := "b0011".U
        awprot   := "b000".U
        awqos    := 0.U
        awregion := 0.U
        awvalid  := that.wrAddr.valid
        that.wrAddr.ready := awready

        wdata  := that.wrData.bits.data
        wstrb  := that.wrData.bits.strb
        wlast  := that.wrData.bits.last
        wvalid := that.wrData.valid
        that.wrData.ready := wready
    
        that.wrResp.bits.id   := bid
        that.wrResp.bits.resp := bresp
        that.wrResp.valid     := bvalid
        bready := that.wrResp.ready
    }
}