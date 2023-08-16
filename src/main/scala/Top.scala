import chisel3._

class Top extends Module {
  val io = IO(new TrinityIO())

  val resetSync = ~RegNext(RegNext(reset.asBool))
  withClockAndReset (clock, resetSync) {
    val trinity = Module(new Trinity())
    io <> trinity.io
  }
}

object Main extends App {
  emitVerilog(new Top(), Array("--target-dir", "generated"))
}