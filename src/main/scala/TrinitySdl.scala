import chisel3._

class TrinitySdl extends Module {
  val io = IO(new TrinityIO {
    val pos = Output(new ScreenPos)
    val active = Output(Bool())
  });

  val vgaSignal = Module(new VgaSignal())
  io := vgaSignal.io
  val shader = Module(new Shader())
  shader.io.pos := vgaSignal.io.pos
  io.pix := shader.io.pix
}
