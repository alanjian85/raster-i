#pragma once

#include <cstdint>

struct RGB8 {
    uint8_t r, g, b;

    RGB8() = default;

    RGB8(uint8_t r, uint8_t g, uint8_t b) {
        this->r = r;
        this->g = g;
        this->b = b;
    }

    uint32_t encode() const {
        return r | static_cast<uint32_t>(g) << 8 |
               static_cast<uint32_t>(b) << 16;
    }

    static RGB8 decode(uint32_t val) { return RGB8(val, val >> 8, val >> 16); }
};
