#pragma once

#include <algorithm>
#include <utility>

#include <math/vec.hpp>
#include <mesh.hpp>
#include <utils/aabb.hpp>

template <typename T> struct Triangle2 {
    Vec2<T> vertices[3];

    Triangle2() = default;

    Triangle2(Vec2<T> a, Vec2<T> b, Vec2<T> c) {
        vertices[0] = a;
        vertices[1] = b;
        vertices[2] = c;
    }

    Aabb2<T> aabb() const {
        auto min_x =
            std::min(std::min(vertices[0].x, vertices[1].x), vertices[2].x);
        auto min_y =
            std::min(std::min(vertices[0].y, vertices[1].y), vertices[2].y);
        auto max_x =
            std::max(std::max(vertices[0].x, vertices[1].x), vertices[2].x);
        auto max_y =
            std::max(std::max(vertices[0].y, vertices[1].y), vertices[2].y);
        return Aabb2<T>(Vec2<T>(min_x, min_y), Vec2<T>(max_x, max_y));
    }

    T signed_area() const {
        return (vertices[1].x - vertices[0].x) *
                   (vertices[2].y - vertices[0].y) -
               (vertices[1].y - vertices[0].y) *
                   (vertices[2].x - vertices[0].x);
    }

    std::pair<bool, Vec3f> barycentric(Vec2<T> p) const {
        T area = abs(
            (vertices[1].x - vertices[0].x) * (vertices[2].y - vertices[0].y) -
            (vertices[1].y - vertices[0].y) * (vertices[2].x - vertices[0].x));

        T edge0 = (vertices[2].x - vertices[1].x) * (p.y - vertices[1].y) -
                  (vertices[2].y - vertices[1].y) * (p.x - vertices[1].x);
        T edge1 = (vertices[0].x - vertices[2].x) * (p.y - vertices[2].y) -
                  (vertices[0].y - vertices[2].y) * (p.x - vertices[2].x);
        T edge2 = (vertices[1].x - vertices[0].x) * (p.y - vertices[0].y) -
                  (vertices[1].y - vertices[0].y) * (p.x - vertices[0].x);
        Vec3<T> edge(edge0, edge1, edge2);

        if (edge0 > 0) {
            return std::make_pair(edge1 > 0 && edge2 > 0,
                                  Vec3f(edge) / static_cast<float>(area));
        } else if (edge0 < 0) {
            edge = -edge;
            return std::make_pair(edge1 < 0 && edge2 < 0,
                                  Vec3f(edge) / static_cast<float>(area));
        }

        return std::make_pair(false, Vec3f());
    }
};

using Triangle2i = Triangle2<int>;