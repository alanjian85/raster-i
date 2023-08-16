import chisel3._
import chisel3.util._

class Trinity extends Module {
  val io = IO(new Bundle {
    val led = Output(Bool())
  })

  val ledReg = RegInit(false.B)
  io.led := ledReg
  val CNT_MAX = 100000000
  val cntReg = RegInit(0.U(log2Up(CNT_MAX).W))
  cntReg := cntReg + 1.U
  when (cntReg === (CNT_MAX - 1).U) {
    ledReg := !ledReg
    cntReg := 0.U
  }
}

object Main extends App {
  emitVerilog(new Trinity(), Array("--target-dir", "generated"))
}
