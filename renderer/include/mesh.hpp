#pragma once

#include <math/vec.hpp>

struct Vertex {
    Vec3f pos;
};

#define NR_MESH_VERTICES 507
#define NR_MESH_TRIANGLES 968

extern const Vertex MESH_VERTICES[NR_MESH_VERTICES];
extern const int MESH_INDICES[3 * NR_MESH_TRIANGLES];
