// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module vertex_shader(
        input clk_pix,
        output reg [9:0] ax = 320,
        output [9:0] ay,
        output reg [9:0] bx = 112,
        output [9:0] by,
        output reg [9:0] cx = 528,
        output [9:0] cy
    );

    assign ay = 60;
    assign by = 420;
    assign cy = 420;

    reg [17:0] cnt = 0;
    reg inc = 1;
    always @(posedge clk_pix) begin
         if (cnt == 18'd225_000) begin
            cnt <= 0;
            if (inc) begin
                ax <= ax + 1;
                bx <= bx + 1;
                cx <= cx + 1;
                if (cx == 640)
                    inc <= 0;
            end else begin
                ax <= ax - 1;
                bx <= bx - 1;
                cx <= cx - 1;
                if (bx == 0)
                    inc <= 1;
            end
        end else begin
            cnt <= cnt + 1;
        end
    end

endmodule
