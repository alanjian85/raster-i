#include <algorithm>
#include <cfloat>

#include <fb.hpp>
#include <math/math.hpp>
#include <math/triangle.hpp>
#include <mem_layout.hpp>
#include <mesh.hpp>
#include <texture.hpp>
#include <utils/aabb.hpp>
#include <utils/color.hpp>

static Vec2i transformed_vertices[NR_MESH_VERTICES];
static Aabb2i bounding_boxes[NR_MESH_TRIANGLES];

Vertex interpolate_vertices(int idx, Vec3f bary) {
    Vertex vertex_a = MESH_VERTICES[idx];
    Vertex vertex_b = MESH_VERTICES[idx + 1];
    Vertex vertex_c = MESH_VERTICES[idx + 2];

    Vertex vertex;
    vertex.uv =
        vertex_a.uv * bary.x + vertex_b.uv * bary.y + vertex_c.uv * bary.z;
    return vertex;
}

void draw_triangle(Vec2i pos, uint32_t *tile, int idx) {
    Triangle2i triangle(transformed_vertices[idx],
                        transformed_vertices[idx + 1],
                        transformed_vertices[idx + 2]);
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
            std::pair<bool, Vec3f> bary =
                triangle.barycentric(Vec2i(pos.x + x, pos.y + y));
            if (bary.first) {
                Vertex vertex = interpolate_vertices(idx, bary.second);
                tile[y * FB_TILE_WIDTH + x] =
                    sample_texture(vertex.uv).encode();
            }
        }
    }
}

void trinity_renderer(fb_id_t fb_id, ap_uint<128> *vram, ap_uint<9> angle) {
#pragma HLS INTERFACE mode = ap_ctrl_hs port = return
#pragma HLS INTERFACE mode = m_axi port = vram offset = off

    ap_uint<128> *fb =
        vram + FB_OFFSET + (static_cast<uint32_t>(fb_id) << FB_ID_SHIFT);

    float sine = SINE_TABLE[angle];
    float cosine = COSINE_TABLE[angle];
    Vec3f axis(0.5f / sqrt(1.25f), 1.0f / sqrt(1.25f), 0.0f);

    for (int i = 0; i < NR_MESH_VERTICES; i++) {
        Vec3f pos = MESH_VERTICES[i].pos;
        Vec3f vc = axis * dot(pos, axis);
        Vec3f v1 = pos - vc;
        Vec3f v2 = cross(v1, axis);
        pos = vc + v1 * cosine + v2 * sine;
        pos.z += 2;
        transformed_vertices[i] =
            Vec2i((1 + pos.x / pos.z * 0.75f) * FB_WIDTH / 2,
                  (1 - pos.y / pos.z) * FB_HEIGHT / 2);

        if (i % 3 == 2) {
            Triangle2i triangle(transformed_vertices[i - 2],
                                transformed_vertices[i - 1],
                                transformed_vertices[i]);
            bounding_boxes[i / 3] = triangle.aabb();
        }
    }

    for (int y = 0; y < FB_HEIGHT; y += FB_TILE_HEIGHT) {
        for (int x = 0; x < FB_WIDTH; x += FB_TILE_WIDTH) {
            uint32_t tile[FB_TILE_WIDTH * FB_TILE_HEIGHT] = {};
            Aabb2i tile_aabb(Vec2i(x, y),
                             Vec2i(x + FB_TILE_WIDTH, y + FB_TILE_HEIGHT));
            for (int i = 0; i < NR_MESH_VERTICES; i += 3) {
                if (!bounding_boxes[i / 3].overlap(tile_aabb))
                    continue;
                draw_triangle(Vec2i(x, y), tile, i);
            }
            fb_write_tile(fb, Vec2i(x, y), tile);
        }
    }
}
