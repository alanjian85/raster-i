// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module shader(
        input visible,
        input [19:0] ua,
        input [19:0] va,
        input [19:0] wa,
        input [19:0] a,
        output [3:0] r,
        output [3:0] g,
        output [3:0] b
    );

    wire [3:0] bar_r = {ua, 4'h0} / a;
    wire [3:0] bar_g = {va, 4'h0} / a;
    wire [3:0] bar_b = {wa, 4'h0} / a;

    assign r = visible ? bar_r : 4'h1;
    assign g = visible ? bar_g : 4'h3;
    assign b = visible ? bar_b : 4'h7;

endmodule
