#include <algorithm>
#include <cfloat>
#include <cstdlib>

#include <fb.hpp>
#include <math/math.hpp>
#include <math/triangle.hpp>
#include <mem_layout.hpp>
#include <mesh.hpp>
#include <texture.hpp>
#include <utils/aabb.hpp>
#include <utils/color.hpp>

static Vec2i transformed_vertices[NR_MESH_VERTICES];
static float transformed_depths[NR_MESH_VERTICES];
static Aabb2i bounding_boxes[NR_MESH_TRIANGLES];

static void render_triangle(uint32_t *tile, float *zbuf, Vec2i pos, int i) {
    MeshIndex idx = MESH_INDICES[i];
    Triangle2i triangle(transformed_vertices[idx.vertices.x],
                        transformed_vertices[idx.vertices.y],
                        transformed_vertices[idx.vertices.z]);
    int area = ((triangle.vertices[1].x - triangle.vertices[0].x) * (triangle.vertices[2].y - triangle.vertices[0].y) - 
               (triangle.vertices[1].y - triangle.vertices[0].y) * (triangle.vertices[2].x - triangle.vertices[0].x));
    if (area <= 0)
        return;

    Vec3i bary_row = triangle.barycentric(pos);
    float z_row = transformed_depths[idx.vertices.x] * bary_row.x + transformed_depths[idx.vertices.y] * bary_row.y + transformed_depths[idx.vertices.z] * bary_row.z; 
    RGB8 n_row = MESH_NORMALS[idx.normals.x] * bary_row.x + MESH_NORMALS[idx.normals.y] * bary_row.y + MESH_NORMALS[idx.normals.z] * bary_row.z;

    int d0 = triangle.vertices[2].x - triangle.vertices[1].x;
    int d1 = triangle.vertices[2].y - triangle.vertices[1].y;
    int d2 = triangle.vertices[0].x - triangle.vertices[2].x;
    int d3 = triangle.vertices[0].y - triangle.vertices[2].y;
    int d4 = triangle.vertices[1].x - triangle.vertices[0].x;
    int d5 = triangle.vertices[1].y - triangle.vertices[0].y;

    float dz_u = transformed_depths[idx.vertices.x] * d1 + transformed_depths[idx.vertices.y] * d3 + transformed_depths[idx.vertices.z] * d5;
    float dz_v = transformed_depths[idx.vertices.x] * d0 + transformed_depths[idx.vertices.y] * d2 + transformed_depths[idx.vertices.z] * d4;

    z_row /= area;
    dz_u /= area;
    dz_v /= area;

    RGB8 dn_u = MESH_NORMALS[idx.normals.x] * d1 + MESH_NORMALS[idx.normals.y] * d3 + MESH_NORMALS[idx.normals.z] * d5;
    RGB8 dn_v = MESH_NORMALS[idx.normals.x] * d0 + MESH_NORMALS[idx.normals.y] * d2 + MESH_NORMALS[idx.normals.z] * d4;

    n_row = n_row / area;
    dn_u = dn_u / area;
    dn_v = dn_v /  area;
    
render_y:
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        Vec3i bary = bary_row;
        float z = z_row;
        RGB8 n = n_row;

    render_x:
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
            if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0 && z <= zbuf[y * FB_TILE_WIDTH + x]) {
                tile[y * FB_TILE_WIDTH + x] = n.encode();
                zbuf[y * FB_TILE_WIDTH + x] = z;
            }
            bary = bary - Vec3i(d1, d3, d5);
            z = z - dz_u;
            n = n - dn_u;
        }
        
        bary_row = bary_row + Vec3i(d0, d2, d4);
        z_row = z_row + dz_v;
        n_row = n_row + dn_v;
    }
}

void trinity_renderer(fb_id_t fb_id, hls::burst_maxi<ap_uint<128>> vram,
                      ap_uint<9> angle) {
#pragma HLS INTERFACE mode = ap_ctrl_hs port = return
#pragma HLS INTERFACE mode = m_axi port = vram offset = off

    float sine = SINE_TABLE[angle];
    float cosine = COSINE_TABLE[angle];
    Vec3f axis(0.0f, 1.0f, 0.0f);

preproc_vertices:
    for (int i = 0; i < NR_MESH_VERTICES; i++) {
#pragma HLS PIPELINE off
        Vec3f pos = MESH_VERTICES[i];
        Vec3f vc = axis * dot(pos, axis);
        Vec3f v1 = pos - vc;
        Vec3f v2 = cross(v1, axis);
        pos = vc + v1 * cosine + v2 * sine;
        pos.z += 2;
        transformed_vertices[i] =
            Vec2i((1 + pos.x / pos.z * 0.75f) * FB_WIDTH / 2,
                  (1 - pos.y / pos.z) * FB_HEIGHT / 2);
        transformed_depths[i] = pos.z;
    }

preproc_triangles:
    for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
#pragma HLS PIPELINE off
        Vec3i idx = MESH_INDICES[i].vertices;
        Triangle2i triangle(transformed_vertices[idx.x],
                            transformed_vertices[idx.y],
                            transformed_vertices[idx.z]);
        bounding_boxes[i] = triangle.aabb();
    }

render_tile_y:
    for (int y = 0; y < FB_HEIGHT; y += FB_TILE_HEIGHT) {
    render_tile_x:
        for (int x = 0; x < FB_WIDTH; x += FB_TILE_WIDTH) {
            Aabb2i aabb(Vec2i(x, y),
                        Vec2i(x + FB_TILE_WIDTH, y + FB_TILE_HEIGHT));
            uint32_t tile[FB_TILE_WIDTH * FB_TILE_HEIGHT];
            float zbuf[FB_TILE_WIDTH * FB_TILE_HEIGHT];
        clear_tile:
            for (int i = 0; i < FB_TILE_WIDTH * FB_TILE_HEIGHT; i++) {
#pragma HLS PIPELINE off
                tile[i] = 0xFF000000;
                zbuf[i] = FLT_MAX;
            }

        render_triangles:
            for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
                if (bounding_boxes[i].overlap(aabb)) {
                    render_triangle(tile, zbuf, Vec2i(x, y), i);
                }
            }

            fb_write_tile(Vec2i(x, y), tile);
        }
        fb_flush_tiles(vram, fb_id, y);
    }
}
