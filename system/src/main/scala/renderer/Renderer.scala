import chisel3._
import chisel3.util._

class renderer extends BlackBox {
  val io = IO(new Bundle {
    val ap_start = Input(Bool())
    val ap_done = Output(Bool())
    val ap_ready = Output(Bool())
    val ap_clk = Input(Bool())
    val ap_rst_n = Input(Bool())
    val fb_id = Input(UInt(Fb.idWidth.W))
    val angle = Input(UInt(9.W))
    val m_axi_vram = Flipped(new WrAxiExtUpper(32, 128))
  })
}

class Renderer extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool())
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = Flipped(new WrAxiExtUpper(32, 128))
  })
  
  val start = RegInit(true.B)
  val done = RegInit(false.B)

  val renderer = Module(new renderer)
  renderer.io.ap_clk   := clock.asBool
  renderer.io.ap_rst_n := !reset.asBool
  renderer.io.ap_start := start
  io.done              := done
  renderer.io.fb_id    := io.fbId
  renderer.io.m_axi_vram <> io.vram

  when (io.fbId =/= RegNext(io.fbId)) {
  	start := true.B
  	done := false.B
  }
  when (renderer.io.ap_ready) {
  	start := false.B
  }
  when (renderer.io.ap_done) {
  	done := true.B
  }
  
  val cntReg = RegInit(0.U(unsignedBitLength(925924).W))
  val angleReg = RegInit(0.U(9.W))
  cntReg := cntReg + 1.U
  when (cntReg === 925924.U) {
    cntReg := 0.U
    angleReg := angleReg + 1.U
    when (angleReg === 359.U) {
      angleReg := 0.U
    }
  }  
  renderer.io.angle := angleReg
}
