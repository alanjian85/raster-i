import chisel3._

class Renderer extends Module {
  val io = IO(new Bundle {
    val x = Input(UInt(11.W))
    val y = Input(UInt(11.W))
    val r = Output(UInt(4.W))
    val g = Output(UInt(4.W))
    val b = Output(UInt(4.W))
  })

  val square = (99.U <= io.x && io.x <= 699.U) && (199.U <= io.y && io.y <= 399.U)
  io.r := Mux(square, "hf".U, 0.U)
  io.g := Mux(square, "hf".U, 0.U)
  io.b := Mux(square, "hf".U, 0.U)
}
