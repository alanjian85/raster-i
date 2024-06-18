#pragma once

#include <math/vec.hpp>

template <typename T> struct Aabb2 {
    Vec2<T> min, max;

    Aabb2() = default;

    Aabb2(Vec2<T> min, Vec2<T> max) {
        this->min = min;
        this->max = max;
    }
};

using Aabb2i = Aabb2<int>;