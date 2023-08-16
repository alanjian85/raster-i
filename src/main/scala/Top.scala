import chisel3._

class Top extends Module {
  val io = IO(new Bundle {
    val led = Output(Bool())
  })

  val resetSync = ~RegNext(RegNext(reset.asBool))
  withClockAndReset (clock, resetSync) {
    val trinity = Module(new Trinity())
    io.led := trinity.io.led
  }
}

object Main extends App {
  emitVerilog(new Top(), Array("--target-dir", "generated"))
}
