#include <hls_stream.h>
#include <ap_axi_sdata.h>
#include <stdint.h>
#include <math.h>
#include "trinity.hpp"

ap_axiu<24, 1, 1, 1> video;

void trinity(hls::stream<ap_axiu<24, 1, 1, 1>>& m_axis_video, float sine) {
#pragma HLS interface mode=axis port=m_axis_video register_mode=both
#pragma HLS interface mode=s_axilite port=sine
#pragma HLS interface mode=s_axilite port=return

	float cosine = sqrt(1 - sine * sine);
	vec2 a(0.0, 0.86), b(-1.0, -0.86), c(1.0, -0.86);

	a = a.x * vec2(cosine, sine) + a.y * vec2(-sine, cosine);
	b = b.x * vec2(cosine, sine) + b.y * vec2(-sine, cosine);
	c = c.x * vec2(cosine, sine) + c.y * vec2(-sine, cosine);

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

			vec2 coord;
			coord.x = static_cast<float>((j << 1) - WIDTH) / HEIGHT;
			coord.y = (1 - static_cast<float>(i) / (HEIGHT - 1)) * 2 - 1;
			vec3 bary = barycentric(a, b, c, coord);
			uint8_t color_r = 0;
			uint8_t color_g = 0;
			uint8_t color_b = 0;
			if (bary.x >= 0 && bary.y >= 0 && bary.z >= 0) {
				color_r = bary.x * 255;
				color_g = bary.y * 255;
				color_b = bary.z * 255;
			}

			video.data = color_r << 16 | color_g << 8 | color_b;
			m_axis_video << video;
		}
	}
}
