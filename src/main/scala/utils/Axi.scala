// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._

object Axi {
  object Burst {
    val fixed = "b00".U
    val incr  = "b01".U
    val wrap  = "b10".U
  }

  object size {
    def apply(sz: Int) = sz match {
      case 1   => "b000".U
      case 2   => "b001".U
      case 4   => "b010".U
      case 8   => "b011".U
      case 16  => "b100".U
      case 32  => "b101".U
      case 64  => "b110".U
      case 128 => "b111".U
    }
  }
}

class AxiRdAddr(val addrWidth: Int, val idWidth: Int = 0) extends Bundle {
  val id    = UInt(idWidth.W)
  val addr  = UInt(addrWidth.W)
  val len   = UInt(8.W)
  val size  = UInt(3.W)
  val burst = UInt(2.W)
}

class AxiRdData(val dataWidth: Int, val idWidth: Int = 0) extends Bundle {
  val id   = UInt(idWidth.W)
  val data = UInt(dataWidth.W)
  val resp = UInt(2.W)
  val last = Bool()
}

class AxiWrAddr(val addrWidth: Int, val idWidth: Int = 0) extends Bundle {
  val id    = UInt(idWidth.W)
  val addr  = UInt(addrWidth.W)
  val len   = UInt(8.W)
  val size  = UInt(3.W)
  val burst = UInt(2.W)
}

class AxiWrData(val dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
  val strb = UInt((dataWidth / 8).W)
  val last = Bool()
}

class AxiWrResp(val idWidth: Int = 0) extends Bundle {
  val id   = UInt(idWidth.W)
  val resp = UInt(2.W)
}

class RdAxi(val addrWidth: Int, val dataWidth: Int, val idWidth: Int = 0) extends Bundle {
  val addr = Irrevocable(new AxiRdAddr(addrWidth, idWidth))
  val data = Flipped(Irrevocable(new AxiRdData(dataWidth, idWidth)))
}

class WrAxi(val addrWidth: Int, val dataWidth: Int, val idWidth: Int = 0) extends Bundle {
  val addr = Irrevocable(new AxiWrAddr(addrWidth, idWidth))
  val data = Irrevocable(new AxiWrData(dataWidth))
  val resp = Flipped(Irrevocable(new AxiWrResp(idWidth)))
}

class RdWrAxi(val addrWidth: Int, val dataWidth: Int, val idWidth: Int = 0) extends Bundle {
  val rd = new RdAxi(addrWidth, dataWidth, idWidth)
  val wr = new WrAxi(addrWidth, dataWidth, idWidth)
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

class WrAxiExtUpper(addrWidth: Int, dataWidth: Int, idWidth: Int = 0) extends Bundle {
  val AWID     = Input(UInt(idWidth.W))
  val AWADDR   = Input(UInt(addrWidth.W))
  val AWLEN    = Input(UInt(8.W))
  val AWSIZE   = Input(UInt(3.W))
  val AWBURST  = Input(UInt(2.W))
  val AWLOCK   = Input(Bool())
  val AWCACHE  = Input(UInt(4.W))
  val AWPROT   = Input(UInt(3.W))
  val AWQOS    = Input(UInt(4.W))
  val AWREGION = Input(UInt(4.W))
  val AWVALID  = Input(Bool())
  val AWREADY  = Output(Bool())

  val WDATA  = Input(UInt(dataWidth.W))
  val WSTRB  = Input(UInt((dataWidth / 8).W))
  val WLAST  = Input(Bool())
  val WVALID = Input(Bool())
  val WREADY = Output(Bool())

  val BID    = Output(UInt(idWidth.W))
  val BRESP  = Output(UInt(2.W))
  val BVALID = Output(Bool())
  val BREADY = Input(Bool())

  def connect(that: WrAxiExt) = {
    that.awid     := AWID
    that.awaddr   := AWADDR
    that.awlen    := AWLEN
    that.awsize   := AWSIZE
    that.awburst  := AWBURST
    that.awlock   := AWLOCK
    that.awcache  := AWCACHE
    that.awprot   := AWPROT
    that.awqos    := AWQOS
    that.awregion := AWREGION
    that.awvalid  := AWVALID
    AWREADY := that.awready

    that.wdata  := WDATA
    that.wstrb  := WSTRB
    that.wlast  := WLAST
    that.wvalid := WVALID
    WREADY := that.wready

    BID    := that.bid
    BRESP  := that.bresp
    BVALID := that.bvalid
    that.bready := BREADY
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
    arid     := that.rd.addr.bits.id
    araddr   := that.rd.addr.bits.addr
    arlen    := that.rd.addr.bits.len
    arsize   := that.rd.addr.bits.size
    arburst  := that.rd.addr.bits.burst
    arlock   := false.B
    arcache  := "b0011".U
    arprot   := "b000".U
    arqos    := 0.U
    arregion := 0.U
    arvalid  := that.rd.addr.valid
    that.rd.addr.ready := arready

    that.rd.data.bits.id   := rid
    that.rd.data.bits.data := rdata
    that.rd.data.bits.resp := rresp
    that.rd.data.bits.last := rlast
    that.rd.data.valid     := rvalid
    rready := that.rd.data.ready

    awid     := that.wr.addr.bits.id
    awaddr   := that.wr.addr.bits.addr
    awlen    := that.wr.addr.bits.len
    awsize   := that.wr.addr.bits.size
    awburst  := that.wr.addr.bits.burst
    awlock   := false.B
    awcache  := "b0011".U
    awprot   := "b000".U
    awqos    := 0.U
    awregion := 0.U
    awvalid  := that.wr.addr.valid
    that.wr.addr.ready := awready

    wdata  := that.wr.data.bits.data
    wstrb  := that.wr.data.bits.strb
    wlast  := that.wr.data.bits.last
    wvalid := that.wr.data.valid
    that.wr.data.ready := wready

    that.wr.resp.bits.id   := bid
    that.wr.resp.bits.resp := bresp
    that.wr.resp.valid     := bvalid
    bready := that.wr.resp.ready
  }
}
