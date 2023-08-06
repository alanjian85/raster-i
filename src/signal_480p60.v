// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module signal_480p60(
        input clk_pix,
        input resetn,
        output reg [9:0] x,
        output reg [9:0] y,
        output hsync,
        output vsync,
        output active
    );

    localparam HA_END = 639;
    localparam HF_END = HA_END + 16;
    localparam HS_END = HF_END + 64;
    localparam HB_END = HS_END + 80;

    localparam VA_END = 479;
    localparam VF_END = VA_END + 3;
    localparam VS_END = VF_END + 4;
    localparam VB_END = VS_END + 13;

    assign hsync = !(HF_END < x && x <= HS_END);
    assign vsync = !(VF_END < y && y <= VS_END);
    assign active = x <= HA_END && y <= VA_END;

    always @(posedge clk_pix, negedge resetn) begin
        if (!resetn) begin
            x <= 0;
            y <= 0;
        end else begin
            if (x == HB_END) begin
                x <= 0;
                y <= y == VB_END ? 0 : y + 1;
            end else begin
                x <= x + 1;
            end
        end
    end

endmodule
