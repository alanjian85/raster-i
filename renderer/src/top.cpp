#include <algorithm>
#include <cstdlib>

#include <fb.hpp>
#include <hls_math.h>
#include <math/math.hpp>
#include <math/triangle.hpp>
#include <mem_layout.hpp>
#include <mesh.hpp>
#include <utils/aabb.hpp>
#include <utils/color.hpp>

static Vec3f rotate_vec(Vec3f v, Vec3f axis, fixed sine, fixed cosine) {
    Vec3f vc = axis * dot(v, axis);
    Vec3f v1 = v - vc;
    Vec3f v2 = cross(v1, axis);
    return vc + v1 * cosine + v2 * sine;
}

struct ScreenVertex {
    Vec2i pos;
    fixed z;
};

static ScreenVertex screen_vertices[NR_MESH_VERTICES];
static Vec3f transformed_positions[NR_MESH_VERTICES];
static Vec3f transformed_normals[NR_MESH_NORMALS];
static Aabb2i bounding_boxes[NR_MESH_TRIANGLES];

static void preproc(ap_uint<9> angle) {
    fixed sine = SINE_TABLE[angle];
    fixed cosine = COSINE_TABLE[angle];
    Vec3f axis(0.0f, 1.0f, 0.0f);

preproc_vertices:
    for (int i = 0; i < NR_MESH_VERTICES; i++) {
#pragma HLS PIPELINE off
        Vec3f pos = rotate_vec(MESH_VERTICES[i], axis, sine, cosine);
        pos.z += 2;
        screen_vertices[i].pos =
            Vec2i((1 + pos.x / pos.z * 3 / 4) * FB_WIDTH / 2,
                  (1 - pos.y / pos.z) * FB_HEIGHT / 2);
        screen_vertices[i].z = pos.z;
        transformed_positions[i] = pos;
    }

preproc_normals:
    for (int i = 0; i < NR_MESH_NORMALS; i++) {
#pragma HLS PIPELINE off
        Vec3f normal = rotate_vec(MESH_NORMALS[i], axis, sine, cosine);
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
}

struct PixAttrib {
    Vec3f pos;
    Vec3f n;
};

static void render_triangle(fixed (*zbuf)[FB_SAMPLES_PER_PIXEL],
                            PixAttrib (*buf)[FB_SAMPLES_PER_PIXEL], Vec2i pos,
                            MeshIndex idx) {
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
    fixed z_orig = screen_vertices[idx.vertices.x].z * bary_orig.x +
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

    Vec3i dbary_x(d1, d3, d5);
    Vec3i dbary_y(d0, d2, d4);
    Vec3i dbary_u = dbary_x / 2;
    Vec3i dbary_v = dbary_y / 2;

    fixed dz_x = screen_vertices[idx.vertices.x].z * dbary_x.x +
                 screen_vertices[idx.vertices.y].z * dbary_x.y +
                 screen_vertices[idx.vertices.z].z * dbary_x.z;
    fixed dz_y = screen_vertices[idx.vertices.x].z * dbary_y.x +
                 screen_vertices[idx.vertices.y].z * dbary_y.y +
                 screen_vertices[idx.vertices.z].z * dbary_y.z;

    z_orig /= area;
    dz_x /= area;
    dz_y /= area;
    fixed dz_u = dz_x / 2;
    fixed dz_v = dz_y / 2;

    Vec3f dpos_x = transformed_positions[idx.vertices.x] * dbary_x.x +
                   transformed_positions[idx.vertices.y] * dbary_x.y +
                   transformed_positions[idx.vertices.z] * dbary_x.z;
    Vec3f dpos_y = transformed_positions[idx.vertices.x] * dbary_y.x +
                   transformed_positions[idx.vertices.y] * dbary_y.y +
                   transformed_positions[idx.vertices.z] * dbary_y.z;

    pos_orig /= area;
    dpos_x /= area;
    dpos_y /= area;

    Vec3f dn_x = transformed_normals[idx.normals.x] * dbary_x.x +
                 transformed_normals[idx.normals.y] * dbary_x.y +
                 transformed_normals[idx.normals.z] * dbary_x.z;
    Vec3f dn_y = transformed_normals[idx.normals.x] * dbary_y.x +
                 transformed_normals[idx.normals.y] * dbary_y.y +
                 transformed_normals[idx.normals.z] * dbary_y.z;

    n_orig /= area;
    dn_x /= area;
    dn_y /= area;

render_y:
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
    render_x:
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
#pragma HLS PIPELINE
#pragma HLS UNROLL factor = 8
#pragma HLS ARRAY_PARTITION variable = zbuf dim = 1 type = cyclic factor = 8
#pragma HLS ARRAY_PARTITION variable = buf dim = 1 type = cyclic factor = 8

            Vec3i bary = bary_orig - dbary_x * x + dbary_y * y;
            fixed z = z_orig - dz_x * x + dz_y * y;

            ap_uint<FB_SAMPLES_PER_PIXEL> we = 0;

            Vec3i bary00 = bary;
            fixed z00 = z;
            if (bary00.x >= 0 && bary00.y >= 0 && bary00.z >= 0 &&
                z00 <= zbuf[y * FB_TILE_WIDTH + x][0]) {
                zbuf[y * FB_TILE_WIDTH + x][0] = z00;
                we |= 1;
            }

            Vec3i bary10 = bary00 - dbary_u;
            fixed z10 = z00 - dz_u;
            if (bary10.x >= 0 && bary10.y >= 0 && bary10.z >= 0 &&
                z10 <= zbuf[y * FB_TILE_WIDTH + x][1]) {
                zbuf[y * FB_TILE_WIDTH + x][1] = z10;
                we |= 2;
            }

            Vec3i bary11 = bary10 + dbary_v;
            fixed z11 = z10 + dz_v;
            if (bary11.x >= 0 && bary11.y >= 0 && bary11.z >= 0 &&
                z11 <= zbuf[y * FB_TILE_WIDTH + x][2]) {
                zbuf[y * FB_TILE_WIDTH + x][2] = z11;
                we |= 4;
            }

            Vec3i bary01 = bary11 + dbary_u;
            fixed z01 = z11 + dz_u;
            if (bary01.x >= 0 && bary01.y >= 0 && bary01.z >= 0 &&
                z01 <= zbuf[y * FB_TILE_WIDTH + x][3]) {
                zbuf[y * FB_TILE_WIDTH + x][3] = z01;
                we |= 8;
            }

            if (we) {
#pragma HLS ARRAY_PARTITION variable = buf dim = 2 type = complete

                PixAttrib attrib;
                attrib.pos = pos_orig - dpos_x * x + dpos_y * y;
                attrib.n = n_orig - dn_x * x + dn_y * y;

                for (int i = 0; i < FB_SAMPLES_PER_PIXEL; i++) {
                    if (we & 1 << i) {
                        buf[y * FB_TILE_WIDTH + x][i] = attrib;
                    }
                }
            }
        }
    }
}

static void deferred_shading(uint32_t *tile,
                             PixAttrib (*buf)[FB_SAMPLES_PER_PIXEL]) {
    for (int y = 0; y < FB_TILE_HEIGHT; y++) {
        for (int x = 0; x < FB_TILE_WIDTH; x++) {
            RGB8 rgb(0, 0, 0);
            for (int i = 0; i < FB_SAMPLES_PER_PIXEL; i++) {
#pragma HLS PIPELINE
                Vec3f pos = buf[y * FB_TILE_WIDTH + x][i].pos;
                Vec3f n = buf[y * FB_TILE_WIDTH + x][i].n;

                Vec3f dir = Vec3f(0, 0, 0) - pos;
                fixed intensity = dot(dir, n);
                if (intensity < 0)
                    intensity = 0;
                intensity /=
                    hls::sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z);

                fixed factor = intensity * 255;

                int r = factor;
                int g = factor;
                int b = factor;

                rgb.r += r;
                rgb.g += g;
                rgb.b += b;
            }

            rgb.r /= FB_SAMPLES_PER_PIXEL;
            rgb.g /= FB_SAMPLES_PER_PIXEL;
            rgb.b /= FB_SAMPLES_PER_PIXEL;

            tile[y * FB_TILE_WIDTH + x] = rgb.encode();
        }
    }
}

void trinity_renderer(fb_id_t fb_id, hls::burst_maxi<ap_uint<128>> vram,
                      ap_uint<9> angle) {
#pragma HLS INTERFACE mode = ap_ctrl_hs port = return
#pragma HLS INTERFACE mode = m_axi port = vram offset = off

    preproc(angle);

render_tile_y:
    for (int y = 0; y < FB_HEIGHT; y += FB_TILE_HEIGHT) {
    render_tile_x:
        for (int x = 0; x < FB_WIDTH; x += FB_TILE_WIDTH) {
            Aabb2i aabb(Vec2i(x, y),
                        Vec2i(x + FB_TILE_WIDTH, y + FB_TILE_HEIGHT));

            uint32_t tile[FB_TILE_WIDTH * FB_TILE_HEIGHT];
            fixed zbuf[FB_TILE_WIDTH * FB_TILE_HEIGHT][FB_SAMPLES_PER_PIXEL];
            PixAttrib buf[FB_TILE_WIDTH * FB_TILE_HEIGHT][FB_SAMPLES_PER_PIXEL];

        clear_tile:
            for (int i = 0; i < FB_TILE_WIDTH * FB_TILE_HEIGHT; i++) {
                tile[i] = 0;
                for (int j = 0; j < FB_SAMPLES_PER_PIXEL; j++) {
                    zbuf[i][j] = 1000;
                    buf[i][j].n = Vec3f(0, 0, 0);
                }
            }

        render_triangles:
            for (int i = 0; i < NR_MESH_TRIANGLES; i++) {
                if (bounding_boxes[i].overlap(aabb)) {
                    render_triangle(zbuf, buf, Vec2i(x, y), MESH_INDICES[i]);
                }
            }

            deferred_shading(tile, buf);

            fb_write_tile(Vec2i(x, y), tile);
        }
        fb_flush_tiles(vram, fb_id, y);
    }
}
