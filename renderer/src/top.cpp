#include <cstddef>
#include <cstdint>
#include <tuple>
#include <algorithm>
#include <cmath>
#include <iostream>
using std::min;
using std::max;

#include <float.h>
#include <hls_burst_maxi.h>

#include "color.hpp"
#include "fb.hpp"
#include "trig.hpp"
#include "img.hpp"
#include "math.hpp"
#include "aabb.hpp"

const int fb_width = 1024;
const int fb_height = 768;

struct RGBA8x4 {
    RGBA8 e[4];

    static RGBA8x4 decode(ap_uint<128> vec) {
        return RGBA8x4 { .e = {RGBA8::decode(vec), RGBA8::decode(vec >> 32), RGBA8::decode(vec >> 64), RGBA8::decode(vec >> 96) } };
    }

    ap_uint<128> encode() const {
        return e[0].encode() | (static_cast<ap_uint<128>>(e[1].encode()) << 32) | (static_cast<ap_uint<128>>(e[2].encode()) << 64) | (static_cast<ap_uint<128>>(e[3].encode()) << 96);
    }
};

RGBA8 filter(float u, float v) {
    int x = ceil(u * 235);
    int y = ceil(v * 235);

    auto rgb = image[y * 236 + x];
    return RGBA8 { .r = rgb.r, .g = rgb.g, .b = rgb.b, .a = 0xFF };
}

void draw_triangle(int r, int c, RGBA8 *buf, float *zbuf, const Triangle &triangle, const AABB &aabb) {
    if (!aabb.overlap(AABB(c, r, c + 16, r + 16)))
        return;
        
    for (int i = 0; i < 16; i++) {
        for (int j = 0; j < 16; j++) {
            auto bary = triangle.barycentric(c + j, r + i);

            FbColor color;

            auto area = triangle.area();
            float a = static_cast<float>(std::get<1>(bary)) / area;
            float b = static_cast<float>(std::get<2>(bary)) / area;
            float c = static_cast<float>(std::get<3>(bary)) / area;

            float z = 1 / (triangle.z[0] * a + triangle.z[1] * b + triangle.z[2] * c);
            float u = (a * triangle.u[0] + b * triangle.u[1] + c * triangle.u[2]) * z;
            float v = (a * triangle.v[0] + b * triangle.v[1] + c * triangle.v[2]) * z;

            if (std::get<0>(bary) && z < zbuf[i * 16 + j]) {
                int x = u * 236;
                int y = v * 236;
                buf[i * fb_width + j] = filter(u, v);
                zbuf[i * 16 + j] = z;
            }
        }
    }
}

void write_buf(fb_id_t fb_id, hls::burst_maxi<ap_uint<128>> fb, int r, RGBA8 *buf) {
    for (int i = 0; i < 16; i++) {
        RGBA8x4 temp[256];
        for (int j = 0; j < 256; j++) {
            temp[j].e[0] = buf[i * 1024 + j * 4 + 0];
            temp[j].e[1] = buf[i * 1024 + j * 4 + 1];
            temp[j].e[2] = buf[i * 1024 + j * 4 + 2];
            temp[j].e[3] = buf[i * 1024 + j * 4 + 3];
        }

        fb.write_request(((static_cast<uint32_t>(fb_id) << 20) + (r + i) * fb_width) / 4, 256);

        for (int j = 0; j < 256; j++) {
#pragma HLS PIPELINE II=1
            fb.write(temp[j].encode());
        }
        
        fb.write_response();
    }
}

void write_buf_sim(fb_id_t fb_id, ap_uint<128> *fb, int r, RGBA8 *buf) {
    for (int i = 0; i < 1024 * 16; i += 4) {
        RGBA8x4 vec;
        vec.e[0] = buf[i];
        vec.e[1] = buf[i + 1];
        vec.e[2] = buf[i + 2];
        vec.e[3] = buf[i + 3];
        fb[(r * 1024 + i) / 4] = vec.encode();
    }
}

#ifdef __SYNTHESIS__
void trinity_renderer(fb_id_t fb_id, hls::burst_maxi<ap_uint<128>> vram, ap_uint<9> angle) {
#else
void trinity_renderer(fb_id_t fb_id, ap_uint<128> *vram, ap_uint<9> angle) {
#endif
#pragma HLS INTERFACE mode=ap_ctrl_hs port=return
#pragma HLS INTERFACE mode=m_axi port=vram offset=off

    float sine = ::sine[angle];
    float cosine = ::cosine[angle];

    FTriangle ftriangles[12];
    build_triangles(ftriangles);

    const float ax = 0.5 / sqrt(1.25);
    const float ay = 1.0 / sqrt(1.25);
    const float az = 0.0;

    for (int i = 0; i < 12; i++) {
        for (int j = 0; j < 3; j++) {
#pragma HLS PIPELINE
            float px = ftriangles[i].x[j];
            float py = ftriangles[i].y[j];
            float pz = ftriangles[i].z[j];

            float dot = px * ax + py * ay + pz * az;
            float vcx = ax * dot;
            float vcy = ay * dot;
            float vcz = az * dot;

            float v1x = px - vcx;
            float v1y = py - vcy;
            float v1z = pz - vcz;            

            float v2x = v1y * az - v1z * ay;
            float v2y = v1z * ax - v1x * az;
            float v2z = v1x * ay - v1y * ax;

            float x = vcx + v1x * cosine + v2x * sine;
            float y = vcy + v1y * cosine + v2y * sine;
            float z = vcz + v1z * cosine + v2z * sine;

            ftriangles[i].x[j] = x;
            ftriangles[i].y[j] = y;
            ftriangles[i].z[j] = z + 2;
        }
    }

    Triangle triangles[12];
    AABB bounding_boxes[12];
    for (int i = 0; i < 12; i++) {
        for (int j = 0; j < 3; j++) {
#pragma HLS PIPELINE
            triangles[i].x[j] = (1 + ftriangles[i].x[j] / ftriangles[i].z[j] * 0.75) * fb_width / 2;
            triangles[i].y[j] = (1 - ftriangles[i].y[j] / ftriangles[i].z[j]) * fb_height / 2;
            triangles[i].z[j] = 1 / ftriangles[i].z[j];
            triangles[i].u[j] = ftriangles[i].u[j] / ftriangles[i].z[j];
            triangles[i].v[j] = ftriangles[i].v[j] / ftriangles[i].z[j];
        }

        bounding_boxes[i] = AABB(triangles[i]);
    }

    for (int i = 0; i < fb_height; i += 16) {
        FbColor buf[1024 * 16] = { 0 };
        for (int j = 0; j < fb_width; j += 16) {
            float zbuf[16 * 16];
            for (int i = 0; i < 256; i++)
                zbuf[i] = FLT_MAX;
            for (int k = 0; k < 12; k++) {
#pragma HLS DATAFLOW
                draw_triangle(i, j, buf + j, zbuf, triangles[k], bounding_boxes[k]);
            }
        }
        #ifdef __SYNTHESIS__
            write_buf(fb_id, vram, i, buf);
        #else
            write_buf_sim(fb_id, vram, i, buf);
        #endif
    }
}
