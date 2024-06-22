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
static int num_triangles = 0;
static Aabb2i bounding_boxes[NR_MESH_TRIANGLES];
static int triangle_indices[NR_MESH_TRIANGLES * 3];

static Vertex interpolate_vertices(int i, Vec3f bary) {
    int idx0 = MESH_INDICES[i * 3];
    int idx1 = MESH_INDICES[i * 3 + 1];
    int idx2 = MESH_INDICES[i * 3 + 2];

    Vertex vertex_a = MESH_VERTICES[idx0];
    Vertex vertex_b = MESH_VERTICES[idx1];
    Vertex vertex_c = MESH_VERTICES[idx2];

    Vertex vertex;
    // vertex.uv =
    //     vertex_a.uv * bary.x + vertex_b.uv * bary.y + vertex_c.uv * bary.z;
    return vertex;
}

static void draw_triangle(uint32_t *tile, Vec2i pos, Aabb2i aabb, int i) {
    if (!bounding_boxes[i].overlap(aabb))
        return;

    int idx0 = triangle_indices[i * 3 + 0];
    int idx1 = triangle_indices[i * 3 + 1];
    int idx2 = triangle_indices[i * 3 + 2];
    Triangle2i triangle(transformed_vertices[idx0], transformed_vertices[idx1],
                        transformed_vertices[idx2]);

    for (int y = 0; y < FB_TILE_WIDTH; y++) {
        for (int x = 0; x < FB_TILE_HEIGHT; x++) {
#pragma HLS UNROLL factor = 8
#pragma HLS ARRAY_PARTITION variable = tile type = cyclic factor = 8
            std::pair<bool, Vec3f> bary =
                triangle.barycentric(Vec2i(pos.x + x, pos.y + y));
            if (bary.first) {
                // Vertex vertex = interpolate_vertices(i, bary.second);
                tile[y * FB_TILE_WIDTH + x] = 0xFFFFFFFF;
                // sample_texture(vertex.uv).encode();
            }
        }
    }
}

void trinity_renderer(fb_id_t fb_id, hls::burst_maxi<ap_uint<128>> vram,
                      ap_uint<9> angle) {
#pragma HLS INTERFACE mode = ap_ctrl_hs port = return
#pragma HLS INTERFACE mode = m_axi port = vram offset = off

    float sine = SINE_TABLE[angle];
    float cosine = COSINE_TABLE[angle];
    Vec3f axis(0.0f, 1.0f, 0.0f);

    for (int i = 0; i < NR_MESH_VERTICES; i++) {
#pragma HLS PIPELINE off
        Vec3f pos = MESH_VERTICES[i].pos;
        Vec3f vc = axis * dot(pos, axis);
        Vec3f v1 = pos - vc;
        Vec3f v2 = cross(v1, axis);
        pos = vc + v1 * cosine + v2 * sine;
        pos.z += 2;
        transformed_vertices[i] =
            Vec2i((1 + pos.x / pos.z * 0.75f) * FB_WIDTH / 2,
                  (1 - pos.y / pos.z) * FB_HEIGHT / 2);
    }

    for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
#pragma HLS PIPELINE off
        int idx0 = MESH_INDICES[i * 3];
        int idx1 = MESH_INDICES[i * 3 + 1];
        int idx2 = MESH_INDICES[i * 3 + 2];
        Triangle2i triangle(transformed_vertices[idx0],
                            transformed_vertices[idx1],
                            transformed_vertices[idx2]);
        if (triangle.signed_area() < 0)
            continue;

        int idx = num_triangles++;
        bounding_boxes[idx] = triangle.aabb();
        triangle_indices[idx * 3 + 0] = idx0;
        triangle_indices[idx * 3 + 1] = idx1;
        triangle_indices[idx * 3 + 2] = idx2;
    }

    for (int y = 0; y < FB_HEIGHT; y += FB_TILE_HEIGHT) {
        for (int x = 0; x < FB_WIDTH; x += FB_TILE_WIDTH) {
            uint32_t tile[FB_TILE_WIDTH * FB_TILE_HEIGHT] = {};
            Aabb2i aabb(Vec2i(x, y),
                        Vec2i(x + FB_TILE_WIDTH, y + FB_TILE_HEIGHT));
            for (int i = 0; i < num_triangles; i++) {
                draw_triangle(tile, Vec2i(x, y), aabb, i);
            }
            fb_write_tile(Vec2i(x, y), tile);
        }
        fb_flush_tiles(vram, fb_id, y);
    }
}
