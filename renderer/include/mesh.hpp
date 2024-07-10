#pragma once

#include <math/vec.hpp>
#include <utils/color.hpp>

struct MeshIndex {
    Vec3i vertices;
    Vec3i normals;
};

#define NR_MESH_VERTICES 1501
#define NR_MESH_NORMALS 2998
#define NR_MESH_TRIANGLES 2998

extern const Vec3f MESH_VERTICES[NR_MESH_VERTICES];
extern const Vec3f MESH_NORMALS[NR_MESH_NORMALS];
extern const MeshIndex MESH_INDICES[NR_MESH_TRIANGLES];
