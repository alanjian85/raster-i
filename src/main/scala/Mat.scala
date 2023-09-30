// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class UMat4(eWidth: Int) extends Bundle {
  val elems = Vec(4, Vec(4, UInt(eWidth.W)))

  def apply(idx: Int)  = elems(idx)
  def apply(idx: UInt) = elems(idx)
}

class UMat4Factory(eWidth: Int) {
  def apply() = new UMat4(eWidth)

  def apply(e00: Int, e01: Int, e02: Int, e03: Int,
            e10: Int, e11: Int, e12: Int, e13: Int,
            e20: Int, e21: Int, e22: Int, e23: Int,
            e30: Int, e31: Int, e32: Int, e33: Int)
  = {
    val result = Wire(new UMat4(eWidth))
    result(0)(0) := e00.U
    result(0)(1) := e01.U
    result(0)(2) := e02.U
    result(0)(3) := e03.U

    result(1)(0) := e10.U
    result(1)(1) := e11.U
    result(1)(2) := e12.U
    result(1)(3) := e13.U

    result(2)(0) := e20.U
    result(2)(1) := e21.U
    result(2)(2) := e22.U
    result(2)(3) := e23.U

    result(3)(0) := e30.U
    result(3)(1) := e31.U
    result(3)(2) := e32.U
    result(3)(3) := e33.U
    result
  }

  def apply(e00: UInt, e01: UInt, e02: UInt, e03: UInt,
            e10: UInt, e11: UInt, e12: UInt, e13: UInt,
            e20: UInt, e21: UInt, e22: UInt, e23: UInt,
            e30: UInt, e31: UInt, e32: UInt, e33: UInt)
  = {
    val result = Wire(Vec(4, Vec(4, UInt(eWidth.W))))
    result(0)(0) := e00
    result(0)(1) := e01
    result(0)(2) := e02
    result(0)(3) := e03

    result(1)(0) := e10
    result(1)(1) := e11
    result(1)(2) := e12
    result(1)(3) := e13

    result(2)(0) := e20
    result(2)(1) := e21
    result(2)(2) := e22
    result(2)(3) := e23

    result(3)(0) := e30
    result(3)(1) := e31
    result(3)(2) := e32
    result(3)(3) := e33
    result
  }
}
