#pragma once

#include <ap_int.h>
#include <cstdint>

#include <hls_burst_maxi.h>

#include <math/vec.hpp>

#define FB_WIDTH 1024
#define FB_HEIGHT 768
#define FB_ID_SHIFT 18

using fb_id_t = ap_uint<1>;

#define FB_TILE_WIDTH 32
#define FB_TILE_HEIGHT 32

void fb_write_tile(Vec2i pos, const uint32_t *tile);
void fb_flush_tiles(hls::burst_maxi<ap_uint<128>> vram, fb_id_t fb_id,
                    int line);