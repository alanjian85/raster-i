// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module vertex_shader(
        input clk_pix,
        output reg [8:0] rom_addr,
        input signed [11:0] sine,
        input signed [11:0] cosine,
        output reg [9:0] ax,
        output reg [9:0] ay,
        output reg [9:0] bx,
        output reg [9:0] by,
        output reg [9:0] cx,
        output reg [9:0] cy
    );

    initial begin
        rom_addr = 0;
    end

    wire signed [18:0] sine120_fixed = 120 * sine;
    wire signed [8:0] sine120 = sine120_fixed[18:10];
    wire signed [18:0] cosine120_fixed = 120 * cosine;
    wire signed [8:0] cosine120 = cosine120_fixed[18:10];

    wire signed [19:0] sine140_fixed = 140 * sine;
    wire signed [9:0] sine140 = sine140_fixed[19:10];
    wire signed [19:0] cosine140_fixed = 140 * cosine;
    wire signed [9:0] cosine140 = cosine140_fixed[19:10];

    always @(sine) begin
        ax = 320 - sine120;
        ay = 240 + cosine120;
        bx = 320 - cosine140 + sine120;
        by = 240 - sine140 - cosine120;
        cx = 320 + cosine140 + sine120;
        cy = 240 + sine140 - cosine120;
    end

    reg [18:0] cnt = 0;
    always @(posedge clk_pix) begin
        if (cnt == 19'd333333) begin
            cnt <= 0;
            if (rom_addr == 9'd359) begin
                rom_addr <= 0;
            end else
                rom_addr <= rom_addr + 1;
        end else begin
            cnt <= cnt + 1;
        end
    end

endmodule
