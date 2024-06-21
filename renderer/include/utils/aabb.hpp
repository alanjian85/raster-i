#pragma once

#include <math/vec.hpp>

template <typename T> struct Aabb2 {
    Vec2<T> min, max;

    Aabb2() = default;

    Aabb2(Vec2<T> min, Vec2<T> max) {
        this->min = min;
        this->max = max;
    }

    bool overlap(const Aabb2 &other) const {
        return !(min.x > other.max.x || min.y > other.max.y ||
                 max.x < other.min.x || max.y < other.min.y);
    }
};

using Aabb2i = Aabb2<int>;
