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
        .clk_100m(clk_100m),
        .resetn(resetn),
        .clk_pix(clk_pix),
        .clk_pix_locked(clk_pix_locked)
    );

    wire [9:0] x, y;
    wire hsync, vsync, active;
    signal_480p60 signal_inst(
        .clk_pix(clk_pix),
        .resetn(clk_pix_locked),
        .x(x),
        .y(y),
        .hsync(hsync),
        .vsync(vsync),
        .active(active)
    );

    wire [8:0] rom_addr;
    wire [11:0] sine;
    sine_rom sine_rom_table (
        .a(rom_addr),
        .spo(sine)
    );

    wire [11:0] cosine;
    cosine_rom cosine_rom_table (
        .a(rom_addr),
        .spo(cosine)
    );

    wire [9:0] ax, ay, bx, by, cx, cy;
    vertex_shader vertex_shader_inst(
        .clk_pix(clk_pix),
        .rom_addr(rom_addr),
        .sine(sine),
        .cosine(cosine),
        .ax(ax),
        .ay(ay),
        .bx(bx),
        .by(by),
        .cx(cx),
        .cy(cy)
    );

    wire [19:0] ua, va, wa, a;
    wire visible;
    rasterizer rasterizer_inst(
        .ax(ax),
        .ay(ay),
        .bx(bx),
        .by(by),
        .cx(cx),
        .cy(cy),
        .x(x),
        .y(y),
        .ua(ua),
        .va(va),
        .wa(wa),
        .a(a),
        .visible(visible)
    );

    wire [3:0] r, g, b;
    fragment_shader fragment_shader_inst(
        .visible(visible),
        .ua(ua),
        .va(va),
        .wa(wa),
        .a(a),
	    .r(r),
	    .g(g),
	    .b(b)
    );

    always @(posedge clk_pix) begin
        vga_r <= active ? r : 4'h0;
        vga_g <= active ? g : 4'h0;
        vga_b <= active ? b : 4'h0;
        vga_hsync <= hsync;
        vga_vsync <= vsync;
    end

endmodule
