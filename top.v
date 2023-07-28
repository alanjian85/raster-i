module top(input clk,
           output led);
  reg [26:0] cnt = 0;
  always @ (posedge clk) begin
    if (cnt == 27'd100_000_000) begin
      cnt <= 0;
      led <= ~led;
    end else begin
      cnt <= cnt + 1;
    end
  end
endmodule
