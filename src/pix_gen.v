(* use_dsp = "yes" *) module pix_gen(
        input [9:0] x,
        input [9:0] y,
        output [3:0] r,
        output [3:0] g,
        output [3:0] b
    );

    localparam AX = 320;
    localparam AY = 60;

    localparam BX = 112;
    localparam BY = 420;
    localparam ABX = BX - AX;
    localparam ABY = BY - AY;

    localparam CX = 528;
    localparam CY = 420;
    localparam ACX = CX - AX;
    localparam ACY = CY - AY;

    localparam ABXACY = ABX * ACY;
    localparam ABYACX = ABY * ACX;
    localparam A = ABXACY - ABYACX;

    wire signed [9:0] apx = x - AX;
    wire signed [9:0] apy = y - AY;

    wire signed [19:0] abxapy = ABX * apy;
    wire signed [19:0] abyapx = ABY * apx;
    wire signed [19:0] va = abxapy - abyapx;
    wire v = va > 0 && A > 0 || va < 0 && A < 0;

    wire signed [19:0] apxacy = apx * ACY;
    wire signed [19:0] apyacx = apy * ACX;
    wire signed [19:0] wa = apxacy - apyacx;
    wire w = wa > 0 && A > 0 || wa < 0 && A < 0;

    wire u = A > 0 && A > va + wa ||
             A < 0 && A < va + wa;

    wire visible = u && v && w;

    assign r = visible ? 4'hF : 4'h1;
    assign g = visible ? 4'hF : 4'h3;
    assign b = visible ? 4'hF : 4'h7;

endmodule
