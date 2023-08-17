import chisel3._

class TrinitySdl extends Module {
  val io = IO(new TrinityIO {
    val x = Output(UInt(11.W))
    val y = Output(UInt(11.W))
    val active = Output(Bool())
  });

  val vgaSignal = Module(new VgaSignal())
  io.hsync := vgaSignal.io.hsync
  io.vsync := vgaSignal.io.vsync
  io.x := vgaSignal.io.x
  io.y := vgaSignal.io.y
  io.active := (vgaSignal.io.x < vgaSignal.HA_END.U) &&
               (vgaSignal.io.y < vgaSignal.VA_END.U)
  val shader = Module(new Shader())
  shader.io.x := vgaSignal.io.x
  shader.io.y := vgaSignal.io.y
  io.r := shader.io.r
  io.g := shader.io.g
  io.b := shader.io.b
}
