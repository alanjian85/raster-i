#pragma once

#include <cstdint>

struct RGB8 {
    uint8_t r;
    uint8_t g;
    uint8_t b;

    uint8_t &operator[](int idx) {
        return idx == 0 ? r : idx == 1 ? g : b;
    }
};

struct RGBA8 {
    uint8_t r;
    uint8_t g;
    uint8_t b;
    uint8_t a;

    static RGBA8 decode(uint32_t vec) {
        RGBA8 result;
        result.r = vec;
        result.g = vec >> 8;
        result.b = vec >> 16;
        result.a = vec >> 24;
        return result;
    }

    uint32_t encode() const {
        return r | g << 8 | b << 16 | a << 24;
    }
};