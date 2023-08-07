// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module vert_shader(
        input clk_pix,
        output reg [8:0] angle,
        input signed [11:0] cos,
        input signed [11:0] y1,
        input signed [11:0] y2,
        output [9:0] ax,
        output [9:0] ay,
        output [9:0] bx,
        output [9:0] by,
        output [9:0] cx,
        output [9:0] cy
    );

    initial begin
        angle = 0;
    end

    assign ax = 320;
    assign ay = 120;

    wire signed [23:0] x1_1 = y1 * cos;
    wire signed [13:0] x1_2 = x1_1[23:10];
    wire signed [21:0] x1_3 = x1_2 * 140;
    wire signed [11:0] x1_4 = x1_3[21:10];
    assign bx = 320 + x1_4;
    wire signed [18:0] y1_1 = y1 * 120;
    wire signed [8:0] y1_2 = y1_1[18:10];
    assign by = 240 + y1_2;

    wire signed [23:0] x2_1 = y2 * -cos;
    wire signed [13:0] x2_2 = x2_1[23:10];
    wire signed [21:0] x2_3 = x2_2 * 140;
    wire signed [11:0] x2_4 = x2_3[21:10];
    assign cx = 320 + x2_4;
    wire signed [18:0] y2_1 = y2 * 120;
    wire signed [8:0] y2_2 = y2_1[18:10];
    assign cy = 240 + y2_2;

    reg [18:0] cnt = 0;
    always @(posedge clk_pix) begin
        if (cnt == 19'd333333) begin
            cnt <= 0;
            if (angle == 9'd359) begin
                angle <= 0;
            end else
                angle <= angle + 1;
        end else begin
            cnt <= cnt + 1;
        end
    end

endmodule
