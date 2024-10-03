#pragma once

#include <cstdint>

struct RGB8 {
    uint32_t r, g, b;

    RGB8() = default;

    RGB8(uint32_t r, uint32_t g, uint32_t b) {
        this->r = r;
        this->g = g;
        this->b = b;
    }

    uint32_t encode() const { return r | g << 8 | b << 16; }

    static RGB8 decode(uint32_t val) { return RGB8(val, val >> 8, val >> 16); }
};

inline RGB8 operator+(const RGB8 &lhs, const RGB8 &rhs) {
    return RGB8(lhs.r + rhs.r, lhs.g + rhs.g, lhs.b + rhs.b);
}

inline RGB8 operator-(const RGB8 &lhs, const RGB8 &rhs) {
    return RGB8(lhs.r - rhs.r, lhs.g - rhs.g, lhs.b - rhs.b);
}

inline RGB8 operator*(const RGB8 &lhs, int rhs) {
    return RGB8(lhs.r * rhs, lhs.g * rhs, lhs.b * rhs);
}

inline RGB8 operator/(const RGB8 &lhs, int rhs) {
    return RGB8(lhs.r / rhs, lhs.g / rhs, lhs.b / rhs);
}

inline RGB8 operator>>(const RGB8 &lhs, int rhs) {
    return RGB8(lhs.r >> rhs, lhs.g >> rhs, lhs.b >> rhs);
}