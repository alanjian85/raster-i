// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module rasterizer(
        input [8:0] ax,
        input [8:0] ay,
        input [8:0] bx,
        input [8:0] by,
        input [6:0] bz,
        input [8:0] cx,
        input [8:0] cy,
        input [6:0] cz,
        input [9:0] x,
        input [9:0] y,
        output [17:0] uw,
        output [17:0] vw,
        output [17:0] ww,
        output [18:0] aw,
        output visible
    );

    wire signed [7:0] abx = bx - ax;
    wire signed [8:0] aby = by - ay;

    wire signed [7:0] acx = cx - ax;
    wire signed [8:0] acy = cy - ay;

    wire signed [9:0] apx = x - ax;
    wire signed [9:0] apy = y - ay;

    wire signed [15:0] abxacy = abx * acy;
    wire signed [15:0] abyacx = aby * acx;
    wire signed [16:0] sa = abxacy - abyacx;
    wire [15:0] a = sa > 0 ? sa : -sa;

    wire signed [17:0] apxacy = apx * acy;
    wire signed [17:0] apyacx = apy * acx;
    wire [18:0] v = sa > 0 ? apxacy - apyacx : apyacx - apxacy;

    wire signed [17:0] abxapy = abx * apy;
    wire signed [17:0] abyapx = aby * apx;
    wire [18:0] w = sa > 0 ? abxapy - abyapx : abyapx - abxapy;

    wire [18:0] u = a - v - w;

    assign uw = u[18:1];
    wire [24:0] vw_fixed = v[17:0] * bz;
    assign vw = vw_fixed[24:7];
    wire [24:0] ww_fixed = w[17:0] * cz;
    assign ww = ww_fixed[24:7];
    assign aw = uw + vw + ww;

    assign visible = !(u[18] || v[18] || w[18] || a == 0);

endmodule
