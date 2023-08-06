// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module vertex_shader(
        input clk_pix,
        output reg [8:0] sine_rom_addr,
        input signed [11:0] sine,
        output reg [9:0] ax,
        output reg [9:0] ay,
        output reg [9:0] bx,
        output reg [9:0] by,
        output reg [9:0] cx,
        output reg [9:0] cy
    );

    initial begin
        sine_rom_addr = 0;
        ax = 320;
        ay = 120;
        bx = 181;
        by = 360;
        cx = 459;
        cy = 360;
    end

    wire signed [19:0] sine180_fixed = 180 * sine;
    wire signed [9:0] sine180 = sine180_fixed[19:10];
    always @(sine) begin
        ax = 320 + sine180;
        bx = 181 + sine180;
        cx = 459 + sine180;
    end

    reg [16:0] cnt = 0;
    always @(posedge clk_pix) begin
        if (cnt == 17'd66667) begin
            cnt <= 0;
            if (sine_rom_addr == 9'd359) begin
                sine_rom_addr <= 0;
            end else
                sine_rom_addr <= sine_rom_addr + 1;
        end else begin
            cnt <= cnt + 1;
        end
    end

endmodule
