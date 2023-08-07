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

    wire [6:0] bz;
    bz_rom bz_rom_inst (
        angle,
        bz
    );

    wire [6:0] cz;
    cz_rom cz_rom_inst (
        angle,
        cz
    );

    wire [8:0] ax;
    wire [6:0] ay;
    wire signed [8:0] aby, acy;
    wire signed [7:0] abx, acx;
    vert_shader vert_shader_inst(
        clk_pix,
        clk_pix_locked,
        angle,
        cos,
        bz,
        cz,
        ax,
        ay,
        abx,
        aby,
        acx,
        acy
    );

    wire [17:0] uw, vw, ww;
    wire [18:0] aw;
    wire visible;
    rasterizer rasterizer_inst(
        ax,
        ay,
        abx,
        aby,
        bz,
        acx,
        acy,
        cz,
        x,
        y,
        uw,
        vw,
        ww,
        aw,
        visible
    );

    wire [3:0] r, g, b;
    frag_shader frag_shader_inst(
        visible,
        uw,
        vw,
        ww,
        aw,
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
