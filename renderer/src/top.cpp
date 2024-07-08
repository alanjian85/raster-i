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
                        transformed_vertices[idx.vertices.z],
                        transformed_vertices[idx.vertices.y]);
    Vec3f depths(transformed_depths[idx.vertices.x],
                 transformed_depths[idx.vertices.y],
                 transformed_depths[idx.vertices.z]);
    Vec3<Vec3f> normals(MESH_NORMALS[idx.normals.x],
                        MESH_NORMALS[idx.normals.y],
                        MESH_NORMALS[idx.normals.z]);

    Vec3i bary_row = triangle.barycentric(pos);
    float inv_area = 1.0f / ((triangle.vertices[1].x - triangle.vertices[0].x) * (triangle.vertices[2].y - triangle.vertices[0].y) -
                        (triangle.vertices[1].y - triangle.vertices[0].y) * (triangle.vertices[2].x - triangle.vertices[0].x));
    int d0 = triangle.vertices[2].x - triangle.vertices[1].x;
    int d1 = triangle.vertices[2].x - triangle.vertices[1].x;
    int d2 = triangle.vertices[0].x - triangle.vertices[2].x;
    int d3 = triangle.vertices[0].x - triangle.vertices[2].x;
    int d4 = triangle.vertices[1].x - triangle.vertices[0].x;
    int d5 = triangle.vertices[1].x - triangle.vertices[0].x;

render_y:
    for (int y = 0; y < FB_TILE_WIDTH; y++) {
        Vec3i bary = bary_row;
    render_x:
        for (int x = 0; x < FB_TILE_HEIGHT; x++) {
//#pragma HLS UNROLL factor=8
//#pragma HLS ARRAY_PARTITION variable=tile type=cyclic factor=8
//#pragma HLS ARRAY_PARTITION variable=zbuf type=cyclic factor=8
            float z = (depths.x * float(bary.x) + depths.y * float(bary.y) +
                      depths.z * float(bary.z)) * inv_area;
            if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0 && z <= zbuf[y * FB_TILE_WIDTH + x]) {
                Vec3f normal = (normals.x * float(bary.x) +
                               normals.y * float(bary.y) +
                               normals.z * float(bary.z)) * inv_area;
                RGB8 rgb((normal.x + 1) * 0.5 * 255, (normal.y + 1) * 0.5 * 255,
                         (normal.z + 1) * 0.5 * 255);
                tile[y * FB_TILE_WIDTH + x] = rgb.encode();
                zbuf[y * FB_TILE_WIDTH + x] = z;
            }
            bary = bary - Vec3i(d1, d3, d5);
        }
        bary_row = bary_row + Vec3i(d0, d2, d4);
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
