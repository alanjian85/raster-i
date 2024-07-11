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

struct ScreenVertex {
    Vec2i pos;
    float z;
};

static ScreenVertex screen_vertices[NR_MESH_VERTICES];
static Vec3f transformed_positions[NR_MESH_VERTICES];
static Vec3f transformed_normals[NR_MESH_NORMALS];
static Aabb2i bounding_boxes[NR_MESH_TRIANGLES];

static void render_triangle(float *zbuf, Vec3f *posbuf, Vec3f *nbuf, Vec2i pos,
                            int i) {
    MeshIndex idx = MESH_INDICES[i];
    Triangle2i triangle(screen_vertices[idx.vertices.x].pos,
                        screen_vertices[idx.vertices.y].pos,
                        screen_vertices[idx.vertices.z].pos);
    int area = ((triangle.vertices[1].x - triangle.vertices[0].x) *
                    (triangle.vertices[2].y - triangle.vertices[0].y) -
                (triangle.vertices[1].y - triangle.vertices[0].y) *
                    (triangle.vertices[2].x - triangle.vertices[0].x));
    if (area <= 0)
        return;

    Vec3i bary_orig = triangle.barycentric(pos);
    float z_orig = screen_vertices[idx.vertices.x].z * bary_orig.x +
                   screen_vertices[idx.vertices.y].z * bary_orig.y +
                   screen_vertices[idx.vertices.z].z * bary_orig.z;
    Vec3f pos_orig = transformed_positions[idx.vertices.x] * bary_orig.x +
                     transformed_positions[idx.vertices.y] * bary_orig.y +
                     transformed_positions[idx.vertices.z] * bary_orig.z;
    Vec3f n_orig = transformed_normals[idx.normals.x] * bary_orig.x +
                   transformed_normals[idx.normals.y] * bary_orig.y +
                   transformed_normals[idx.normals.z] * bary_orig.z;

    int d0 = triangle.vertices[2].x - triangle.vertices[1].x;
    int d1 = triangle.vertices[2].y - triangle.vertices[1].y;
    int d2 = triangle.vertices[0].x - triangle.vertices[2].x;
    int d3 = triangle.vertices[0].y - triangle.vertices[2].y;
    int d4 = triangle.vertices[1].x - triangle.vertices[0].x;
    int d5 = triangle.vertices[1].y - triangle.vertices[0].y;

    Vec3i dbary_u(d1, d3, d5);
    Vec3i dbary_v(d0, d2, d4);

    float dz_u = screen_vertices[idx.vertices.x].z * dbary_u.x +
                 screen_vertices[idx.vertices.y].z * dbary_u.y +
                 screen_vertices[idx.vertices.z].z * dbary_u.z;
    float dz_v = screen_vertices[idx.vertices.x].z * dbary_v.x +
                 screen_vertices[idx.vertices.y].z * dbary_v.y +
                 screen_vertices[idx.vertices.z].z * dbary_v.z;

    z_orig /= area;
    dz_u /= area;
    dz_v /= area;

    Vec3f dpos_u = transformed_positions[idx.vertices.x] * dbary_u.x +
                   transformed_positions[idx.vertices.y] * dbary_u.y +
                   transformed_positions[idx.vertices.z] * dbary_u.z;
    Vec3f dpos_v = transformed_positions[idx.vertices.x] * dbary_v.x +
                   transformed_positions[idx.vertices.y] * dbary_v.y +
                   transformed_positions[idx.vertices.z] * dbary_v.z;

    pos_orig /= area;
    dpos_u /= area;
    dpos_v /= area;

    Vec3f dn_u = transformed_normals[idx.normals.x] * dbary_u.x +
                 transformed_normals[idx.normals.y] * dbary_u.y +
                 transformed_normals[idx.normals.z] * dbary_u.z;
    Vec3f dn_v = transformed_normals[idx.normals.x] * dbary_v.x +
                 transformed_normals[idx.normals.y] * dbary_v.y +
                 transformed_normals[idx.normals.z] * dbary_v.z;

    n_orig /= area;
    dn_u /= area;
    dn_v /= area;

render_y:
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
    render_x:
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
#pragma HLS PIPELINE
            // #pragma HLS UNROLL factor = 8
            // #pragma HLS ARRAY_PARTITION variable = tile type = cyclic factor
            // = 8 #pragma HLS ARRAY_PARTITION variable = zbuf type = cyclic
            // factor = 8
            Vec3i bary = bary_orig - dbary_u * x + dbary_v * y;
            float z = z_orig - dz_u * x + dz_v * y;
            Vec3f pos = pos_orig - dpos_u * x + dpos_v * y;
            Vec3f n = n_orig - dn_u * x + dn_v * y;

            if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0 &&
                z <= zbuf[y * FB_TILE_WIDTH + x]) {
                zbuf[y * FB_TILE_WIDTH + x] = z;
                posbuf[y * FB_TILE_WIDTH + x] = pos;
                nbuf[y * FB_TILE_WIDTH + x] = n;
            }
        }
    }
}

static void deferred_shading(uint32_t *tile, Vec3f *posbuf, Vec3f *nbuf) {
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
            Vec3f pos = posbuf[y * FB_TILE_WIDTH + x];
            Vec3f n = nbuf[y * FB_TILE_WIDTH + x];

            Vec3f dir = Vec3f(0, 0, 0) - pos;
            float intensity = std::max(0.0f, dot(dir, n));
            intensity /= sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z);

            RGB8 rgb;
            rgb.r = intensity * 255;
            rgb.g = intensity * 255;
            rgb.b = intensity * 255;

            tile[y * FB_TILE_WIDTH + x] = rgb.encode();
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

preproc_vertices:
    for (int i = 0; i < NR_MESH_VERTICES; i++) {
#pragma HLS PIPELINE off
        Vec3f pos = MESH_VERTICES[i];
        Vec3f vc = axis * dot(pos, axis);
        Vec3f v1 = pos - vc;
        Vec3f v2 = cross(v1, axis);
        pos = vc + v1 * cosine + v2 * sine;
        pos.z += 2;
        screen_vertices[i].pos =
            Vec2i((1 + pos.x / pos.z * 0.75f) * FB_WIDTH / 2,
                  (1 - pos.y / pos.z) * FB_HEIGHT / 2);
        screen_vertices[i].z = pos.z;
        transformed_positions[i] = pos;
    }

preproc_normals:
    for (int i = 0; i < NR_MESH_NORMALS; i++) {
#pragma HLS PIPELINE off
        Vec3f normal = MESH_NORMALS[i];
        Vec3f vc = axis * dot(normal, axis);
        Vec3f v1 = normal - vc;
        Vec3f v2 = cross(v1, axis);
        normal = vc + v1 * cosine + v2 * sine;
        transformed_normals[i] = normal;
    }

preproc_triangles:
    for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
#pragma HLS PIPELINE off
        Vec3i idx = MESH_INDICES[i].vertices;
        Triangle2i triangle(screen_vertices[idx.x].pos,
                            screen_vertices[idx.y].pos,
                            screen_vertices[idx.z].pos);
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
            Vec3f posbuf[FB_TILE_WIDTH * FB_TILE_HEIGHT];
            Vec3f nbuf[FB_TILE_WIDTH * FB_TILE_HEIGHT];

        clear_tile:
            for (int i = 0; i < FB_TILE_WIDTH * FB_TILE_HEIGHT; i++) {
#pragma HLS PIPELINE off
                zbuf[i] = FLT_MAX;
                nbuf[i] = Vec3f(0, 0, 0);
            }

        render_triangles:
            for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
                if (bounding_boxes[i].overlap(aabb)) {
                    render_triangle(zbuf, posbuf, nbuf, Vec2i(x, y), i);
                }
            }

            deferred_shading(tile, posbuf, nbuf);

            fb_write_tile(Vec2i(x, y), tile);
        }
        fb_flush_tiles(vram, fb_id, y);
    }
}
