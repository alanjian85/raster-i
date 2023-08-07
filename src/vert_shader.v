// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module vert_shader(
        input clk_pix,
        output reg [8:0] angle,
        input signed [11:0] cos,
        input signed [8:0] y1,
        input signed [8:0] y2,
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

    wire signed [20:0] x1_fixed = y1 * cos;
    wire signed [10:0] x1 = x1_fixed[20:10];
    assign bx = 320 + x1;
    assign by = 240 + y1;

    wire signed [20:0] x2_fixed = y2 * -cos;
    wire signed [10:0] x2 = x2_fixed[20:10];
    assign cx = 320 + x2;
    assign cy = 240 + y2;

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
