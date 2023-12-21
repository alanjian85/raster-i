import chisel3._

class renderer extends BlackBox {
  val io = IO(new Bundle {
    val ap_clk = Input(Bool())
    val ap_rst_n = Input(Bool())
    val fb_id = Input(UInt(Fb.idWidth.W))
    val m_axi_vram = Flipped(new WrAxiExtUpper(64, 128))
    val done = Output(Bool())
  })
}

class Renderer extends Module {
  val io = IO(new Bundle {
    val fbId = Input(UInt(Fb.idWidth.W))
    val vram = Flipped(new WrAxiExtUpper(64, 128))
    val done = Output(Bool())
  })

  val renderer = Module(new renderer)
  renderer.io.ap_clk   := clock.asBool
  renderer.io.ap_rst_n := !reset.asBool
  renderer.io.fb_id    := io.fbId
  renderer.io.m_axi_vram <> io.vram
  io.done := renderer.io.done
}
