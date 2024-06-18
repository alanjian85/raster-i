#include <fb.hpp>
#include <math/math.hpp>
#include <math/triangle.hpp>
#include <mesh.hpp>
#include <texture.hpp>
#include <utils/aabb.hpp>
#include <utils/color.hpp>

static Vec2i transformed_vertices[NR_MESH_VERTICES];

Vertex interpolate_vertices(int idx, Vec3f bary) {
    Vertex vertex_a = MESH_VERTICES[idx];
    Vertex vertex_b = MESH_VERTICES[idx + 1];
    Vertex vertex_c = MESH_VERTICES[idx + 2];

    Vertex vertex;
    vertex.pos =
        vertex_a.pos * bary.x + vertex_b.pos * bary.y + vertex_c.pos * bary.z;
    vertex.uv =
        vertex_a.uv * bary.x + vertex_b.uv * bary.y + vertex_c.uv * bary.z;
    return vertex;
}

void draw_triangle(uint32_t *fb, int idx) {
    Triangle2i triangle(transformed_vertices[idx],
                        transformed_vertices[idx + 1],
                        transformed_vertices[idx + 2]);
    Aabb2i aabb = triangle.aabb();
    for (int y = aabb.min.y; y <= aabb.max.y; y++) {
        for (int x = aabb.min.x; x <= aabb.max.x; x++) {
            std::pair<bool, Vec3f> bary = triangle.barycentric(Vec2i(x, y));
            if (bary.first) {
                Vertex vertex = interpolate_vertices(idx, bary.second);
                fb[y * FB_WIDTH + x] = sample_texture(vertex.uv).encode();
            }
        }
    }
}

void trinity_renderer(fb_id_t fb_id, uint32_t *vram, ap_uint<9> angle) {
    uint32_t *fb = vram + (static_cast<uint32_t>(fb_id) << FB_ID_SHIFT);
    for (int y = 0; y < FB_HEIGHT; y++) {
        for (int x = 0; x < FB_WIDTH; x++) {
            fb[y * FB_WIDTH + x] = RGB8(0, 0, 0).encode();
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
    }

    for (int i = 0; i < NR_MESH_VERTICES; i += 3) {
        draw_triangle(fb, i);
    }
}
