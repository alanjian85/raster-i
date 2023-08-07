// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module divider_16x(
        input [19:0] dividend,
        input [19:0] divisor,
        output reg [3:0] quotient);

    wire [23:0] dividend_16x = {dividend, 4'h0};

    wire [23:0] multiples [15:0];
    assign multiples[0]  = 0;
    assign multiples[1]  = divisor;
    assign multiples[2]  = {divisor, 1'h0};
    assign multiples[3]  = multiples[1] + multiples[2];
    assign multiples[4]  = {divisor, 2'h0};
    assign multiples[5]  = multiples[1] + multiples[4];
    assign multiples[6]  = multiples[2] + multiples[4];
    assign multiples[7]  = multiples[8] - multiples[1];
    assign multiples[8]  = {divisor, 3'h0};
    assign multiples[9]  = multiples[1] + multiples[8];
    assign multiples[10] = multiples[2] + multiples[8];
    assign multiples[11] = multiples[3] + multiples[8];
    assign multiples[12] = multiples[4] + multiples[8];
    assign multiples[13] = multiples[5] + multiples[8];
    assign multiples[14] = multiples[6] + multiples[8];
    assign multiples[15] = {divisor, 4'h0} - multiples[1];

    always @(dividend, divisor) begin
        if (dividend_16x >= multiples[8]) begin
            if (dividend_16x >= multiples[12]) begin
                if (dividend_16x >= multiples[14]) begin
                    if (dividend_16x >= multiples[15])
                        quotient = 15;
                    else
                        quotient = 14;
                end else begin
                    if (dividend_16x >= multiples[13])
                        quotient = 13;
                    else
                        quotient = 12;
                end
            end else begin
                if (dividend_16x >= multiples[10]) begin
                    if (dividend_16x >= multiples[11])
                        quotient = 11;
                    else
                        quotient = 10;
                end else begin
                    if (dividend_16x >= multiples[9])
                        quotient = 9;
                    else
                        quotient = 8;
                end
            end
        end else begin
            if (dividend_16x >= multiples[4]) begin
                if (dividend_16x >= multiples[6]) begin
                    if (dividend_16x >= multiples[7])
                        quotient = 7;
                    else
                        quotient = 6;
                end else begin
                    if (dividend_16x >= multiples[5])
                        quotient = 5;
                    else
                        quotient = 4;
                end
            end else begin
                if (dividend_16x >= multiples[2]) begin
                    if (dividend_16x >= multiples[3])
                        quotient = 3;
                    else
                        quotient = 2;
                end else begin
                    if (dividend_16x >= multiples[1])
                        quotient = 1;
                    else
                        quotient = 0;
                end
            end
        end
    end

endmodule

module frag_shader(
        input visible,
        input [19:0] ua,
        input [19:0] va,
        input [19:0] wa,
        input [19:0] a,
        output [3:0] r,
        output [3:0] g,
        output [3:0] b
    );

    wire [3:0] bar_r, bar_g, bar_b;
    divider_16x divider_ua_inst(
        .dividend(ua),
        .divisor(a),
        .quotient(bar_r)
    );
    divider_16x divider_va_inst(
        .dividend(va),
        .divisor(a),
        .quotient(bar_g)
    );
    divider_16x divider_wa_inst(
        .dividend(wa),
        .divisor(a),
        .quotient(bar_b)
    );

    assign r = visible ? bar_r : 4'h1;
    assign g = visible ? bar_g : 4'h3;
    assign b = visible ? bar_b : 4'h7;

endmodule
