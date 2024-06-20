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
static float transformed_depths[NR_MESH_VERTICES];

Vertex interpolate_vertices(int idx, Vec3f bary) {
    Vertex vertex_a = MESH_VERTICES[idx];
    Vertex vertex_b = MESH_VERTICES[idx + 1];
    Vertex vertex_c = MESH_VERTICES[idx + 2];

    vertex_a.pos.z = transformed_depths[idx];
    vertex_b.pos.z = transformed_depths[idx + 1];
    vertex_c.pos.z = transformed_depths[idx + 2];

    Vertex vertex;
    // vertex.pos =
    //     vertex_a.pos * bary.x + vertex_b.pos * bary.y + vertex_c.pos *
    //     bary.z;
    // vertex.pos.z = 1 / (1 / vertex_a.pos.z * bary.x + 1 / vertex_b.pos.z *
    // bary.y + 1 / vertex_c.pos.z * bary.z);
    vertex.uv =
        vertex_a.uv * bary.x + vertex_b.uv * bary.y + vertex_c.uv * bary.z;
    return vertex;
}

#define fb(offset) vram[FB_OFFSET + (static_cast<uint32_t>(fb_id) << FB_ID_SHIFT) + offset]

void draw_triangle(uint32_t *vram, fb_id_t fb_id, /*float *zb, */int idx) {
    Triangle2i triangle(transformed_vertices[idx],
                        transformed_vertices[idx + 1],
                        transformed_vertices[idx + 2]);
    Aabb2i aabb = triangle.aabb();
    for (int y = aabb.min.y; y <= aabb.max.y; y++) {
        for (int x = aabb.min.x; x <= aabb.max.x; x++) {
            std::pair<bool, Vec3f> bary = triangle.barycentric(Vec2i(x, y));
            if (bary.first) { // && vertex.pos.z < zb[y * FB_WIDTH + x]) {
                Vertex vertex = interpolate_vertices(idx, bary.second);
                fb(y * FB_WIDTH + x) = sample_texture(vertex.uv).encode();
                // zb[y * FB_WIDTH + x] = vertex.pos.z;
            }
        }
    }
}

void trinity_renderer(fb_id_t fb_id, uint32_t *vram, ap_uint<9> angle) {
#pragma HLS INTERFACE mode = ap_ctrl_hs port = return
#pragma HLS INTERFACE mode = m_axi port = vram offset = off

    for (int y = 0; y < FB_HEIGHT; y++) {
        for (int x = 0; x < FB_WIDTH; x++) {
            fb(y * FB_WIDTH + x) = RGB8(64, 64, 64).encode();
            // zb[y * FB_WIDTH + x] = FLT_MAX;
        }
    }

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
        transformed_depths[i] = pos.z;
    }

    for (int i = 0; i < NR_MESH_VERTICES; i += 3) {
        draw_triangle(vram, fb_id, /*zb, */i);
    }
}
