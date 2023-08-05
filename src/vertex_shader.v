// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

(* use_dsp = "yes" *) module vertex_shader(
        input clk_pix,
        output reg [9:0] ax,
        output reg [9:0] ay,
        output reg [9:0] bx,
        output reg [9:0] by,
        output reg [9:0] cx,
        output reg [9:0] cy
    );

    initial begin
        ax = 320;
        ay = 120;
        bx = 181;
        by = 360;
        cx = 459;
        cy = 360;
    end

    reg [17:0] cnt = 0;
    reg inc_x = 1, inc_y = 1;
    always @(posedge clk_pix) begin
         if (cnt == 18'd225_000) begin
            cnt <= 0;

            if (inc_x) begin
                ax <= ax + 1;
                bx <= bx + 1;
                cx <= cx + 1;
                if (cx == 639)
                    inc_x <= 0;
            end else begin
                ax <= ax - 1;
                bx <= bx - 1;
                cx <= cx - 1;
                if (bx == 0)
                    inc_x <= 1;
            end

            if (inc_y) begin
                ay <= ay + 1;
                by <= by + 1;
                cy <= cy + 1;
                if (cy == 479)
                    inc_y <= 0;
            end else begin
                ay <= ay - 1;
                by <= by - 1;
                cy <= cy - 1;
                if (ay == 0)
                    inc_y <= 1;
            end
        end else begin
            cnt <= cnt + 1;
        end
    end

endmodule
