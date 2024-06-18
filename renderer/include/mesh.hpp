#pragma once

#include <math/vec.hpp>

struct Vertex {
    Vec3f pos;
    Vec2f uv;
};

#define NR_MESH_VERTICES 36
#define NR_MESH_TRIANGLES 12

extern const Vertex MESH_VERTICES[NR_MESH_VERTICES];
