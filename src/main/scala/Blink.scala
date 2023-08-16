import chisel3._
import chisel3.util._

class Blink extends Module {
  val io = IO(new TrinityIO())

  val ledReg = RegInit(false.B)
  io.led := ledReg
  val CNT_MAX = 40000000
  val cntReg = RegInit(0.U(log2Up(CNT_MAX).W))
  cntReg := cntReg + 1.U
  when (cntReg === (CNT_MAX - 1).U) {
    ledReg := !ledReg
    cntReg := 0.U
  }
}
