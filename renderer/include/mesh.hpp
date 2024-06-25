#pragma once

#include <math/vec.hpp>

struct MeshIndex {
    Vec3i vertices;
    Vec3i normals;
};

#define NR_MESH_VERTICES 507
#define NR_MESH_NORMALS 944
#define NR_MESH_TRIANGLES 968

extern const Vec3f MESH_VERTICES[NR_MESH_VERTICES];
extern const Vec3f MESH_NORMALS[NR_MESH_NORMALS];
extern const MeshIndex MESH_INDICES[NR_MESH_TRIANGLES];
