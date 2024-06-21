#pragma once

#include <ap_int.h>
#include <cstdint>

#include <math/vec.hpp>

#define FB_WIDTH 1024
#define FB_HEIGHT 768
#define FB_ID_SHIFT 18

using fb_id_t = ap_uint<1>;

#define FB_TILE_WIDTH 16
#define FB_TILE_HEIGHT 16

void fb_write_tile(ap_uint<128> *fb, Vec2i pos, const uint32_t *tile);
