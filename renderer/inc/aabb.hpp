#pragma once

#include "trig.hpp"

struct AABB {
    int min_x, min_y;
    int max_x, max_y;

    AABB() = default;

    AABB(int min_x, int min_y, int max_x, int max_y) {
        this->min_x = min_x;
        this->min_y = min_y;
        this->max_x = max_x;
        this->max_y = max_y;
    }

    AABB(const Triangle &t) {
        min_x = min(min(t.x[0], t.x[1]), t.x[2]);
        min_y = min(min(t.y[0], t.y[1]), t.y[2]);

        max_x = max(max(t.x[0], t.x[1]), t.x[2]);
        max_y = max(max(t.y[0], t.y[1]), t.y[2]);
    }

    bool overlap(const AABB &other) const {
        return !(other.min_x > max_x || other.max_x < min_x || other.min_y > max_y || other.max_y < min_y);
    }
};