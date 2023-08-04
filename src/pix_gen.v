(* use_dsp = "yes" *) module pix_gen(
        input [9:0] x,
        input [9:0] y,
        output [3:0] r,
        output [3:0] g,
        output [3:0] b
    );
    
    wire signed [9:0] cx = x - 320;
    wire signed [9:0] cy = y - 240;
    
    wire signed [19:0] rsquared = cx * cx + cy * cy;
    wire circle  = rsquared < 10000;
    
    assign r = circle ? 4'hF : 4'h1;
    assign g = circle ? 4'hF : 4'h3;
    assign b = circle ? 4'hF : 4'h7;
    
endmodule
