#pragma once

#include <math/vec.hpp>
#include <utils/color.hpp>

struct MeshIndex {
    Vec3i vertices;
    Vec3i normals;
};

#define NR_MESH_VERTICES 5000
#define NR_MESH_NORMALS 9995
#define NR_MESH_TRIANGLES 9996

extern const Vec3f MESH_VERTICES[NR_MESH_VERTICES];
extern const RGB8 MESH_NORMALS[NR_MESH_NORMALS];
extern const MeshIndex MESH_INDICES[NR_MESH_TRIANGLES];
