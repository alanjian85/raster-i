// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class UMat4(eWidth: Int) extends Bundle {
  val elem = Vec(4, Vec(4, UInt(eWidth.W)))

  def apply(idx: Int)  = elem(idx)
  def apply(idx: UInt) = elem(idx)
}

class UMat4Factory(eWidth: Int) {
  def apply() = new UMat4(eWidth)

  def apply(e00: Int, e01: Int, e02: Int, e03: Int,
            e10: Int, e11: Int, e12: Int, e13: Int,
            e20: Int, e21: Int, e22: Int, e23: Int,
            e30: Int, e31: Int, e32: Int, e33: Int)
  = {
    val res = Wire(new UMat4(eWidth))
    res(0)(0) := e00.U
    res(0)(1) := e01.U
    res(0)(2) := e02.U
    res(0)(3) := e03.U

    res(1)(0) := e10.U
    res(1)(1) := e11.U
    res(1)(2) := e12.U
    res(1)(3) := e13.U

    res(2)(0) := e20.U
    res(2)(1) := e21.U
    res(2)(2) := e22.U
    res(2)(3) := e23.U

    res(3)(0) := e30.U
    res(3)(1) := e31.U
    res(3)(2) := e32.U
    res(3)(3) := e33.U
    res
  }
}
