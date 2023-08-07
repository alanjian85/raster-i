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
        output [6:0] ay,
        output signed [7:0] abx,
        output signed [8:0] aby,
        output signed [7:0] acx,
        output signed [8:0] acy
    );

    assign ax = 320;
    assign ay = 120;

    wire signed [7:0] bz_signed = {1'h0, bz};
    wire signed [17:0] bx_fixed = bz_signed * -cos;
    assign abx = bx_fixed[17:10];
    assign aby = bz + 120;

    wire signed [7:0] cz_signed = {1'h0, cz};
    wire signed [17:0] cx_fixed = cz_signed * cos;
    assign acx = cx_fixed[17:10];
    assign acy = cz + 120;

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
