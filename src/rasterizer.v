// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module rasterizer(
        input [9:0] x,
        input [9:0] y,
        output visible
    );

    localparam AX = 320;
    localparam AY = 60;

    localparam BX = 112;
    localparam BY = 420;
    localparam ABX = BX - AX;
    localparam ABY = BY - AY;

    localparam CX = 528;
    localparam CY = 420;
    localparam ACX = CX - AX;
    localparam ACY = CY - AY;

    localparam ABXACY = ABX * ACY;
    localparam ABYACX = ABY * ACX;
    localparam A = ABXACY - ABYACX;

    wire signed [9:0] apx = x - AX;
    wire signed [9:0] apy = y - AY;

    wire signed [19:0] abxapy = ABX * apy;
    wire signed [19:0] abyapx = ABY * apx;
    wire signed [19:0] va = abxapy - abyapx;
    wire v = va > 0 && A > 0 || va < 0 && A < 0;

    wire signed [19:0] apxacy = apx * ACY;
    wire signed [19:0] apyacx = apy * ACX;
    wire signed [19:0] wa = apxacy - apyacx;
    wire w = wa > 0 && A > 0 || wa < 0 && A < 0;

    wire u = A > 0 && A > va + wa ||
             A < 0 && A < va + wa;

    assign visible = u && v && w;

endmodule
