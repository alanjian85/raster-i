#include <mesh.hpp>

extern const Vertex MESH_VERTICES[NR_MESH_VERTICES] = {
    {Vec3f(-0.5f, -0.5f, -0.5f), Vec2f(0.0f, 0.0f)},
    {Vec3f(-0.5f, -0.5f, +0.5f), Vec2f(0.0f, 0.0f)},
    {Vec3f(-0.5f, +0.5f, -0.5f), Vec2f(0.0f, 1.0f)},
    {Vec3f(-0.5f, +0.5f, +0.5f), Vec2f(0.0f, 1.0f)},
    {Vec3f(+0.5f, -0.5f, -0.5f), Vec2f(1.0f, 0.0f)},
    {Vec3f(+0.5f, -0.5f, +0.5f), Vec2f(1.0f, 0.0f)},
    {Vec3f(+0.5f, +0.5f, -0.5f), Vec2f(1.0f, 1.0f)},
    {Vec3f(+0.5f, +0.5f, +0.5f), Vec2f(0.0f, 1.0f)},
};

extern const int MESH_INDICES[3 * NR_MESH_TRIANGLES] = {
    0, 2, 4,
    2, 4, 6,
    1, 3, 5,
    3, 5, 7,
    0, 2, 1,
    2, 1, 3,
    4, 6, 5,
    6, 5, 7,
    0, 4, 5,
    0, 5, 1,
    2, 6, 7,
    2, 7, 3,
};