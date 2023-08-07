// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module vert_shader(
        input clk_pix,
        input resetn,
        output reg [8:0] angle,
        input signed [11:0] cos,
        input [6:0] bz,
        input [6:0] cz,
        output [8:0] ax,
        output [8:0] ay,
        output [8:0] bx,
        output [8:0] by,
        output [8:0] cx,
        output [8:0] cy
    );

    assign ax = 320;
    assign ay = 120;

    wire signed [7:0] bz_signed = {1'h0, bz};
    wire signed [18:0] bx_fixed = bz_signed * -cos;
    wire signed [8:0] bx_norm = bx_fixed[18:10];
    assign bx = 320 + bx_norm;
    assign by = 240 + bz;

    wire signed [7:0] cz_signed = {1'h0, cz};
    wire signed [18:0] cx_fixed = cz_signed * cos;
    wire signed [8:0] cx_norm = cx_fixed[18:10];
    assign cx = 320 + cx_norm;
    assign cy = 240 + cz;

    reg [18:0] cnt = 0;
    always @(posedge clk_pix, negedge resetn) begin
        if (cnt == 19'd333333) begin
            cnt <= 0;
            if (angle == 9'd359) begin
                angle <= 0;
            end else
                angle <= angle + 1;
        end else begin
            cnt <= cnt + 1;
        end

        if (!resetn) begin
            angle <= 0;
            cnt <= 0;
        end
    end

endmodule
