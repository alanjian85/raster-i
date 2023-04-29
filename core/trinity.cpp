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
	vec2 a(0.0, 0.5), b(-0.5, -0.5), c(0.5, -0.5);

	a = a.x * vec2(cosine, sine) + a.y * vec2(-sine, cosine);
	b = b.x * vec2(cosine, sine) + b.y * vec2(-sine, cosine);
	c = c.x * vec2(cosine, sine) + c.y * vec2(-sine, cosine);

	a.x = (WIDTH - 1) * (a.x * 0.5 + 0.5); a.y = (HEIGHT - 1) * (a.y * 0.5 + 0.5);
	b.x = (WIDTH - 1) * (b.x * 0.5 + 0.5); b.y = (HEIGHT - 1) * (b.y * 0.5 + 0.5);
	c.x = (WIDTH - 1) * (c.x * 0.5 + 0.5); c.y = (HEIGHT - 1) * (c.y * 0.5 + 0.5);

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
