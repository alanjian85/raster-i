#include "trinity.hpp"
#include <common/xf_headers.hpp>

uint32_t framebuffer[WIDTH * HEIGHT];

int main(int argc, char **argv) {
	for (size_t i = 0; i < HEIGHT; ++i) {
		for (size_t j = 0; j < WIDTH; ++j) {
			framebuffer[i * WIDTH + j] = 0x000000ff;
		}
	}
	hls::stream<command> cmds;
	command cmd;

	cmd.cmd = CMD_VERTEX;
	cmd.vertex = vec2(-0.5, 0.5); cmds << cmd;
	cmd.vertex = vec2(-0.5, -0.5); cmds << cmd;
	cmd.vertex = vec2(0.5, -0.5); cmds << cmd;

	cmd.cmd = CMD_VERTEX;
	cmd.vertex = vec2(-0.5, 0.5); cmds << cmd;
	cmd.vertex = vec2(0.5, 0.5); cmds << cmd;
	cmd.vertex = vec2(0.5, -0.5); cmds << cmd;

	cmd.cmd = CMD_END; cmds << cmd;

	trinity(cmds, framebuffer);

	for (size_t i = 0; i < HEIGHT; ++i) {
		for (size_t j = 0; j < WIDTH; ++j) {
			uint32_t pixel = framebuffer[i * WIDTH + j];
			pixel = (pixel & 0xff) << 24 | (pixel & 0xff00) << 8 | (pixel & 0xff0000) >> 8 | pixel >> 24;
			framebuffer[i * WIDTH + j] = pixel;
		}
	}

	cv::Mat mat(HEIGHT, WIDTH, CV_8UC4, framebuffer);
	cv::cvtColor(mat, mat, cv::COLOR_RGBA2BGRA);
	cv::imwrite("image.png", mat);

	return 0;
}
