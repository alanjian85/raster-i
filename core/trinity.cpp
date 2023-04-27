#include <hls_stream.h>
#include <ap_axi_sdata.h>
#include <stdint.h>
#include "trinity.hpp"

ap_axiu<24, 1, 1, 1> video;

void trinity(hls::stream<ap_axiu<24, 1, 1, 1>>& m_axis_video) {
#pragma HLS interface mode=axis port=m_axis_video register_mode=both
	for (int i = 0; i < HEIGHT; i++) {
		for (int j = 0; j < WIDTH; j++) {
			if (i == 0 && j == 0)
				video.user = 1;
			else
				video.user = 0;

			if (j == WIDTH - 1)
				video.last = 1;
			else
				video.last = 0;

			vec2 a(WIDTH / 2, HEIGHT / 4 * 3), b(WIDTH / 4, HEIGHT / 4), c(WIDTH / 4 * 3, HEIGHT / 4);
			vec3 coord = barycentric(a, b, c, vec2(j, HEIGHT - 1 - i));
			uint8_t color_r = 0;
			uint8_t color_g = 0;
			uint8_t color_b = 0;
			if (coord.x >= 0 && coord.y >= 0 && coord.z >= 0) {
				color_r = coord.x * 255;
				color_g = coord.y * 255;
				color_b = coord.z * 255;
			}

			video.data = color_r << 16 | color_g << 8 | color_b;

			m_axis_video << video;
		}
	}
}
