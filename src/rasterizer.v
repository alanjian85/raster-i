// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module rasterizer(
        input [9:0] ax,
        input [9:0] ay,
        input [9:0] bx,
        input [9:0] by,
        input [9:0] cx,
        input [9:0] cy,
        input [9:0] x,
        input [9:0] y,
        output [19:0] ua,
        output [19:0] va,
        output [19:0] wa,
        output [19:0] a,
        output visible
    );

    wire signed [9:0] abx = bx - ax;
    wire signed [9:0] aby = by - ay;

    wire signed [9:0] acx = cx - ax;
    wire signed [9:0] acy = cy - ay;

    wire signed [9:0] apx = x - ax;
    wire signed [9:0] apy = y - ay;

    wire signed [19:0] abxacy = abx * acy;
    wire signed [19:0] abyacx = aby * acx;
    wire signed [19:0] sa = abxacy - abyacx;
    assign a = sa > 0 ? sa : -sa;

    wire signed [19:0] abxapy = abx * apy;
    wire signed [19:0] abyapx = aby * apx;
    assign va = sa > 0 ? abxapy - abyapx : abyapx - abxapy;

    wire signed [19:0] apxacy = apx * acy;
    wire signed [19:0] apyacx = apy * acx;
    assign wa = sa > 0 ? apxacy - apyacx : apyacx - apxacy;

    assign ua = a - va - wa;

    assign visible = !(ua[19] || va[19] || wa[19] || a == 0);

endmodule
