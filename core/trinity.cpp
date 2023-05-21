#include "trinity.hpp"

#include <cmath>
#include <algorithm>

void do_cmd_vertex(vec2 vertex, volatile uint32_t* framebuffer) {
	static vec2 vertices[3];
	static int index = 0;

	vertices[index++] = vertex;
	index %= 3;

	if (index != 0)
		return;

	for (int i = 0; i < HEIGHT; i++) {
		for (int j = 0; j < WIDTH; j++) {
			vec2 coord;
			coord.x = (static_cast<float>(j * 2) - WIDTH) / HEIGHT;
			coord.y = (1 - static_cast<float>(i) / (HEIGHT - 1)) * 2 - 1;
			vec3 bary = barycentric(vertices[0], vertices[1], vertices[2], coord);
			if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
				uint8_t color_r = bary.x * 255;
				uint8_t color_g = bary.y * 255;
				uint8_t color_b = bary.z * 255;
				framebuffer[i * WIDTH + j] = color_r | color_g << 8 | color_b << 16 | 0xff << 24;
			}
		}
	}
}

void trinity(hls::stream<command>& s_axis_cmds, volatile uint32_t* m_axi_mm_video) {
#pragma HLS INTERFACE mode=m_axi port=m_axi_mm_video offset=slave depth=WIDTH*HEIGHT
#pragma HLS INTERFACE mode=axis register_mode=both port=s_axis_cmds
	bool end = false;
	while (!end) {
		command cmd = s_axis_cmds.read();
		switch (cmd.data & 0xffffffff) {
		case CMD_VERTEX: {
			uint32_t x = cmd.data >> 32 & 0xffffffff;
			uint32_t y = cmd.data >> 64 & 0xffffffff;
			do_cmd_vertex(vec2(bits2float(x), bits2float(y)), m_axi_mm_video);
			break;
		}
		case CMD_END:
			end = true;
			break;
		}
	}
}
