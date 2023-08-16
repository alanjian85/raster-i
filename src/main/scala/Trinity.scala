import chisel3._

class TrinityIO extends Bundle {
  val r = Output(UInt(4.W))
  val g = Output(UInt(4.W))
  val b = Output(UInt(4.W))
  val hsync = Output(Bool())
  val vsync = Output(Bool())
}

class Trinity extends Module {
  val io = IO(new TrinityIO())

  val pixelClock = Module(new PixelClock())
  pixelClock.io.clock := clock
  withClockAndReset(pixelClock.io.clk_pix, reset) {
    val vgaSignal = Module(new VgaSignal())
    io.hsync := vgaSignal.io.hsync
    io.vsync := vgaSignal.io.vsync
    val renderer = Module(new Renderer())
    renderer.io.x := vgaSignal.io.x
    renderer.io.y := vgaSignal.io.y
    io.r := renderer.io.r
    io.g := renderer.io.g
    io.b := renderer.io.b
  }
}
