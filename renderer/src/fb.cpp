#include <fb.hpp>

static uint32_t tile_buf[FB_WIDTH * FB_TILE_HEIGHT];

void fb_write_tile(Vec2i pos, const uint32_t *tile) {
write_tile_x:
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
    write_tile_y:
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
#pragma HLS PIPELINE off
            tile_buf[y * FB_WIDTH + pos.x + x] = tile[y * FB_TILE_WIDTH + x];
        }
    }
}

void fb_flush_tiles(hls::burst_maxi<ap_uint<128>> vram, fb_id_t fb_id,
                    int line) {
flush_tiles_y:
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        vram.write_request((static_cast<uint32_t>(fb_id) << FB_ID_SHIFT) +
                               (line + y) * FB_WIDTH / 4,
                           FB_WIDTH / 4);
    flush_tiles_x:
        for (int x = 0; x < FB_WIDTH; x += 4) {
            ap_uint<128> val = tile_buf[y * FB_WIDTH + x + 0];
            val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 1])
                   << 32;
            val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 2])
                   << 64;
            val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 3])
                   << 96;
            vram.write(val);
        }
        vram.write_response();
    }
}