#include "trinity.hpp"

#include <hls_stream.h>
#include <cstdint>
#include <cmath>
#include <algorithm>

void do_cmd_vertex(vec2 vertex, volatile uint32_t* framebuffer) {
	static vec2 vertices[3];
	static size_t index = 0;

	vertices[index++] = vertex;
	index %= 3;

	if (index != 0)
		return;

	for (size_t i = 0; i < HEIGHT; i++) {
		for (size_t j = 0; j < WIDTH; j++) {
			vec2 coord;
			coord.x = (static_cast<float>(j << 1) - WIDTH) / HEIGHT;
			coord.y = (1 - static_cast<float>(i) / (HEIGHT - 1)) * 2 - 1;

			vec3 bary = barycentric(vertices[0], vertices[1], vertices[2], coord);
			if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
				uint8_t color_r = bary.x * 255;
				uint8_t color_g = bary.y * 255;
				uint8_t color_b = bary.z * 255;
				framebuffer[i * WIDTH + j] = color_r << 24 | color_g << 16 | color_b << 8 | 0xff;
			}
		}
	}
}

void trinity(hls::stream<command>& s_axis_command, volatile uint32_t* m_axi_mm_video) {
#pragma HLS INTERFACE mode=m_axi port=m_axi_mm_video offset=slave depth=WIDTH*HEIGHT
#pragma HLS INTERFACE mode=axis register_mode=both port=s_axis_command
	bool end = false;;
	while (!end) {
		command cmd = s_axis_command.read();
		switch (cmd.cmd) {
		case CMD_VERTEX:
			do_cmd_vertex(cmd.vertex, m_axi_mm_video);
			break;
		case CMD_END:
			end = true;
			break;
		}
	}
}
