import chisel3._
import chisel3.util._
import chisel3.experimental._

class Ddr3Ext extends Bundle {
  val addr    = Output(UInt(14.W))
  val ba      = Output(UInt(3.W))
  val cas_n   = Output(Bool())
  val ck_n    = Output(UInt(1.W))
  val ck_p    = Output(UInt(1.W))
  val cke     = Output(UInt(1.W))
  val cs_n    = Output(UInt(1.W))
  val dm      = Output(UInt(2.W))
  val dq      = Analog(16.W)
  val dqs_n   = Analog(2.W)
  val dqs_p   = Analog(2.W)
  val odt     = Output(UInt(1.W))
  val ras_n   = Output(Bool())
  val reset_n = Output(Bool())
  val we_n    = Output(Bool())
}

object Vram {
  val addrWidth = 28
  val dataWidth = 128

  val dataBytes = dataWidth / 8
  val beatsSize = Axi.size(dataBytes)
}

class vram extends BlackBox {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val reset = Input(Bool())

    val graphics_axi     = new WrAxiExt(Vram.addrWidth, Vram.dataWidth)
    val graphics_aclk    = Input(Bool())
    val graphics_aresetn = Input(Bool())

    val display_axi     = new RdAxiExt(Vram.addrWidth, Vram.dataWidth)
    val display_aclk    = Input(Bool())
    val display_aresetn = Input(Bool())

    val ddr3 = new Ddr3Ext
  })
}

class Vram extends Module {
  val io = IO(new Bundle {
    val axiGraphics   = Flipped(new WrAxi(Vram.addrWidth, Vram.dataWidth))
    val aclkGraphics  = Input(Clock())
    val arstnGraphics = Input(Reset())

    val axiDisplay   = Flipped(new RdAxi(Vram.addrWidth, Vram.dataWidth))
    val aclkDisplay  = Input(Clock())
    val arstnDisplay = Input(Reset())

    val ddr3 = new Ddr3Ext
  })

  val vram = Module(new vram)
  vram.io.clk   := clock.asBool
  vram.io.reset := reset.asBool

  vram.io.graphics_axi.connect(io.axiGraphics)
  vram.io.graphics_aclk    := io.aclkGraphics.asBool
  vram.io.graphics_aresetn := io.arstnGraphics.asBool

  vram.io.display_axi.connect(io.axiDisplay)
  vram.io.display_aclk    := io.aclkDisplay.asBool
  vram.io.display_aresetn := io.arstnDisplay.asBool

  io.ddr3 <> vram.io.ddr3
}

object FbRGB extends RGBFactory(8, 8, 8)

object Fb {
  val idWidth   = 1
  val nrBanks   = Vram.dataWidth / FbRGB.alignedWidth
  val width     = (VgaTiming.width + nrBanks - 1) / nrBanks * nrBanks
  val height    = VgaTiming.height

  val addrWidth = log2Up(Fb.width * Fb.height)
  val lastLine  = width * (height - 1)

  val nrIndices = width / nrBanks
  val maxIdx    = nrIndices - 1
}

class FbSwapper extends Module {
  val io = IO(new Bundle {
    val displayVsync = Input(Bool())
    val graphicsDone = Input(Bool())
    val displayFbId  = Output(UInt(Fb.idWidth.W))
    val graphicsFbId = Output(UInt(Fb.idWidth.W))
  })

  val swapped      = RegInit(false.B)
  val displayFbId  = RegInit(0.U(Fb.idWidth.W))
  val graphicsFbId = RegInit(1.U(Fb.idWidth.W))
  io.displayFbId  := displayFbId
  io.graphicsFbId := graphicsFbId
  when (io.displayVsync && io.graphicsDone) {
    when (!swapped) {
      swapped      := true.B
      displayFbId  := ~displayFbId
      graphicsFbId := ~graphicsFbId
    }
  } .otherwise {
    swapped := false.B
  }
}

class FbRdRes extends Bundle {
  val idx = UInt(log2Up(Fb.nrIndices).W)
  val pix = Vec(Fb.nrBanks, FbRGB())
}

class FbReader extends Module {
  val io = IO(new Bundle {
    val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val fbId  = Input(UInt(Fb.idWidth.W))
    val req   = Input(Bool())
    val res   = Valid(new FbRdRes)
  })

  val addrValid = RegInit(true.B)
  val addr      = RegInit(0.U(Fb.addrWidth.W))
  io.vram.addr.valid      := addrValid
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := ((io.fbId << Fb.addrWidth) ## addr) << log2Up(FbRGB.nrBytes)
  io.vram.addr.bits.len   := Fb.maxIdx.U
  io.vram.addr.bits.size  := Vram.beatsSize
  io.vram.addr.bits.burst := Axi.Burst.incr
  when (io.req) {
    addrValid := true.B
    addr      := addr + Fb.width.U
    when (addr === Fb.lastLine.U) {
      addr := 0.U
    }
  }
  when (addrValid && io.vram.addr.ready) {
    addrValid := false.B
  }

  val idx = RegInit(0.U(log2Up(Fb.nrIndices).W))
  io.vram.data.bits.id := DontCare
  io.vram.data.ready   := true.B
  io.res.valid    := io.vram.data.valid
  io.res.bits.idx := idx
  io.res.bits.pix := VecInit(Seq.tabulate(Fb.nrBanks)(
    i => FbRGB.decode(io.vram.data.bits.data(
      (i + 1) * FbRGB.alignedWidth - 1,
      i * FbRGB.alignedWidth
    ))
  ))
  when (io.vram.data.valid) {
    idx := idx + 1.U
    when (idx === Fb.maxIdx.U) {
      idx  := 0.U
    }
  }
}

class FbWrReq extends Bundle {
  val pix = Vec(Fb.nrBanks, FbRGB())
}

class FbWriter extends Module {
  val io = IO(new Bundle {
    val vram = new WrAxi(Vram.addrWidth, Vram.dataWidth)
    val fbId = Input(UInt(Fb.idWidth.W))
    val req  = Flipped(Irrevocable(new FbWrReq))
    val done = Output(Bool())
  })

  val addrValid = RegInit(false.B)
  val addr      = RegInit(0.U(Fb.addrWidth.W))
  val addrBegan = RegInit(false.B)
  val done      = RegInit(false.B)
  io.vram.addr.valid      := addrValid
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := ((io.fbId << Fb.addrWidth) ## addr) << log2Up(FbRGB.nrBytes)
  io.vram.addr.bits.len   := Fb.maxIdx.U
  io.vram.addr.bits.size  := Vram.beatsSize
  io.vram.addr.bits.burst := Axi.Burst.incr
  when (io.req.valid && !addrBegan) {
    addrBegan := true.B
    addrValid := true.B
    done      := false.B
  }
  when (addrValid && io.vram.addr.ready) {
    addrValid := false.B
    addr      := addr + Fb.width.U
    when (addr === Fb.lastLine.U) {
      addr := 0.U
      done := true.B
    }
  }
  io.done := done

  val idx  = RegInit(0.U(log2Up(Fb.nrIndices).W))
  val last = idx === Fb.maxIdx.U
  io.vram.data.valid     := io.req.valid
  io.vram.data.bits.data := io.req.bits.pix.reverse.map(_.encodeAligned()).reduce(_ ## _)
  io.vram.data.bits.strb := Fill(Vram.dataBytes, 1.U)
  io.vram.data.bits.last := last
  io.req.ready := io.vram.data.ready
  when (io.req.valid && io.vram.data.ready) {
    idx := idx + 1.U
    when (last) {
      idx       := 0.U
      addrBegan := false.B
    }
  }

  io.vram.resp.ready := true.B
}
