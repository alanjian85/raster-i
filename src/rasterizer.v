// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module rasterizer(
        input [9:0] ax,
        input [9:0] ay,
        input [9:0] bx,
        input [9:0] by,
        input [7:0] bz,
        input [9:0] cx,
        input [9:0] cy,
        input [7:0] cz,
        input [9:0] x,
        input [9:0] y,
        output [19:0] uw,
        output [19:0] vw,
        output [19:0] ww,
        output [19:0] aw,
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
    wire [19:0] a = sa > 0 ? sa : -sa;

    wire signed [19:0] apxacy = apx * acy;
    wire signed [19:0] apyacx = apy * acx;
    wire [19:0] v = sa > 0 ? apxacy - apyacx : apyacx - apxacy;

    wire signed [19:0] abxapy = abx * apy;
    wire signed [19:0] abyapx = aby * apx;
    wire [19:0] w = sa > 0 ? abxapy - abyapx : abyapx - abxapy;

    wire [19:0] u = a - v - w;

    assign uw = u[19:1];
    wire [27:0] vw_fixed = v * bz;
    assign vw = vw_fixed[27:7];
    wire [27:0] ww_fixed = w * cz;
    assign ww = ww_fixed[27:7];
    assign aw = uw + vw + ww;

    assign visible = !(u[19] || v[19] || w[19] || a == 0);

endmodule
