module top(
    input clk,
    input rstn,
    output led
);
    reg rstn_sync1, rstn_sync2;
    always @(posedge clk) begin
        rstn_sync1 <= rstn;
        rstn_sync2 <= rstn_sync1;
    end

    Trinity trinity(
        .clock(clk),
        .reset(!rstn_sync2),
        .io_led(led)
    );
endmodule
