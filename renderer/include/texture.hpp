#pragma once

#include <cmath>
#include <cstdint>

#include <math/vec.hpp>
#include <utils/color.hpp>

#define TEXTURE_WIDTH 64
#define TEXTURE_HEIGHT 64

extern const uint32_t TEXTURE[TEXTURE_WIDTH * TEXTURE_HEIGHT];

inline RGB8 sample_texture(Vec2f uv) {
    int x = ceil(uv.x * (TEXTURE_WIDTH - 1));
    int y = ceil(uv.y * (TEXTURE_HEIGHT - 1));

    RGB8 p00 = RGB8::decode(TEXTURE[y * TEXTURE_WIDTH + x]);
    RGB8 p01 = RGB8::decode(TEXTURE[y * TEXTURE_WIDTH + x + 1]);
    RGB8 p10 = RGB8::decode(TEXTURE[(y + 1) * TEXTURE_WIDTH + x]);
    RGB8 p11 = RGB8::decode(TEXTURE[(y + 1) * TEXTURE_WIDTH + x + 1]);

    return (p00 + p01 + p10 + p11) >> 2;
}
