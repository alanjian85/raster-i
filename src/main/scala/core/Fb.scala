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
  val idWidth = 1
  val nrBanks = Vram.dataWidth / FbRGB.alignedWidth
}

class FbSwapper extends Module {
  val io = IO(new Bundle {
    val displayVsync = Input(Bool())
    val graphicsDone = Input(Bool())
    val displayFbId  = Output(UInt(Fb.idWidth.W))
    val graphicsFbId = Output(UInt(Fb.idWidth.W))
  })

  val displayFbId  = RegInit(0.U(Fb.idWidth.W))
  val graphicsFbId = RegInit(1.U(Fb.idWidth.W))
  io.displayFbId  := displayFbId
  io.graphicsFbId := graphicsFbId

  val swapped = RegInit(false.B)
  when (!swapped && io.displayVsync && io.graphicsDone) {
    displayFbId  := ~displayFbId
    graphicsFbId := ~graphicsFbId
    swapped      := true.B
  }
  when (!io.graphicsDone) {
    swapped := false.B
  }
}

class FbRdReq extends Bundle {
  val idx  = UInt(log2Up(VgaTiming.width / Fb.nrBanks).W)
  val line = UInt(log2Up(VgaTiming.height).W)
  val pix  = Vec(Fb.nrBanks, FbRGB())
}

class FbReader extends Module {
  val io = IO(new Bundle {
    val fbId  = Input(UInt(Fb.idWidth.W))
    val pos = Input(TimingPos())
    val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val req   = Valid(new FbRdReq)
  })

  val rdLine = RegInit(0.U(log2Up(VgaTiming.height).W))
  val valid  = RegInit(true.B)
  io.vram.addr.bits.id    := DontCare
  io.vram.addr.bits.addr  := ((io.fbId << log2Up(VgaTiming.width * VgaTiming.height)) |
                              (rdLine << log2Up(VgaTiming.width))) <<
                             log2Up(FbRGB.alignedWidth / 8)
  io.vram.addr.bits.len   := (VgaTiming.width / Fb.nrBanks - 1).U
  io.vram.addr.bits.size  := Axi.size(Vram.dataWidth / 8)
  io.vram.addr.bits.burst := Axi.Burst.incr
  io.vram.addr.valid      := valid
  when (valid && io.vram.addr.ready) {
    valid := false.B
  }

  val rdIdx = RegInit(0.U(log2Up(VgaTiming.width / Fb.nrBanks).W))
  io.vram.data.bits.id := DontCare
  io.vram.data.ready   := true.B
  io.req.valid := false.B
  io.req.bits.pix   := VecInit(Seq.tabulate(Fb.nrBanks)(
    i => FbRGB.decode(io.vram.data.bits.data(
      FbRGB.width + i * FbRGB.alignedWidth - 1,
      i * FbRGB.alignedWidth
    ))
  ))
  when (io.vram.data.valid) {
    io.req.valid := true.B
    rdIdx := rdIdx + 1.U
    when (rdIdx === (VgaTiming.width / Fb.nrBanks - 1).U) {
      rdIdx := 0.U
    }
  }
  io.req.bits.idx := rdIdx

  when (io.pos.y < VgaTiming.height.U && io.pos.x === (VgaTiming.width - 1).U) {
    valid  := true.B
    rdLine := rdLine + 1.U
    when (rdLine === (VgaTiming.height - 1).U) {
      rdLine := 0.U
    }
  }
  io.req.bits.line := rdLine
}
