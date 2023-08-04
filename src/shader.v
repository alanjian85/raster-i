// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: GPL-3.0

module shader(
        input visible,
        output [3:0] r,
        output [3:0] g,
        output [3:0] b
    );

    assign r = visible ? 4'hF : 4'h1;
    assign g = visible ? 4'hF : 4'h3;
    assign b = visible ? 4'hF : 4'h7;

endmodule
