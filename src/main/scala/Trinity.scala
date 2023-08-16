import chisel3._

class TrinityIO extends Bundle {
  val led = Output(Bool())
}

class Trinity extends Module {
  val io = IO(new TrinityIO())

  val pixelClock = Module(new PixelClock())
  pixelClock.io.clock := clock
  withClockAndReset(pixelClock.io.clk_pix, reset) {
    val blink = Module(new Blink())
    io <> blink.io
  }
}
