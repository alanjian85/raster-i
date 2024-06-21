#include <fb.hpp>

static uint32_t tile_buf[FB_WIDTH * FB_TILE_HEIGHT];

void fb_write_tile(ap_uint<128> *fb, Vec2i pos, const uint32_t *tile) {
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
            tile_buf[y * FB_WIDTH + pos.x + x] = tile[y * FB_TILE_WIDTH + x];
        }
    }
    if (pos.x == FB_WIDTH - FB_TILE_WIDTH) {
        for (int y = 0; y < FB_TILE_HEIGHT; y++) {
            for (int x = 0; x < FB_WIDTH; x += 4) {
                ap_uint<128> val = tile_buf[y * FB_WIDTH + x + 0];
                val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 1])
                       << 32;
                val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 2])
                       << 64;
                val |= static_cast<ap_uint<128>>(tile_buf[y * FB_WIDTH + x + 3])
                       << 96;
                fb[((pos.y + y) * FB_WIDTH + x) / 4] = val;
            }
        }
    }
}
