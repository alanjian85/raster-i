// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module top(
        input clk_100m,
        input resetn,
        output reg [3:0] vga_r,
        output reg [3:0] vga_g,
        output reg [3:0] vga_b,
        output reg vga_hsync,
        output reg vga_vsync
    );

    wire clk_pix, clk_pix_locked;
    clock_480p60 clock_inst(
        clk_pix,
        resetn,
        clk_pix_locked,
        clk_100m
    );

    wire [9:0] x, y;
    wire hsync, vsync, active;
    signal_480p signal_inst(
        clk_pix,
        clk_pix_locked,
        x,
        y,
        hsync,
        vsync,
        active
    );

    wire [8:0] angle;
    wire signed [11:0] cos;
    cos_rom cos_rom_inst (
        angle,
        cos
    );

    wire signed [7:0] y1;
    y1_rom y1_rom_inst (
        angle,
        y1
    );

    wire signed [7:0] y2;
    y2_rom y2_rom_inst (
        angle,
        y2
    );

    wire [9:0] ax, ay, bx, by, cx, cy;
    vert_shader vert_shader_inst(
        clk_pix,
        angle,
        cos,
        y1,
        y2,
        ax,
        ay,
        bx,
        by,
        cx,
        cy
    );

    wire [19:0] ua, va, wa, a;
    wire visible;
    rasterizer rasterizer_inst(
        ax,
        ay,
        bx,
        by,
        cx,
        cy,
        x,
        y,
        ua,
        va,
        wa,
        a,
        visible
    );

    wire [3:0] r, g, b;
    frag_shader frag_shader_inst(
        visible,
        ua,
        va,
        wa,
        a,
	    r,
	    g,
	    b
    );

    always @(posedge clk_pix) begin
        vga_r <= active ? r : 4'h0;
        vga_g <= active ? g : 4'h0;
        vga_b <= active ? b : 4'h0;
        vga_hsync <= hsync;
        vga_vsync <= vsync;
    end

endmodule
