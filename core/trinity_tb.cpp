#include "trinity.hpp"
#include <common/xf_headers.hpp>

uint32_t framebuffer[WIDTH * HEIGHT];

int main(int argc, char **argv) {
	for (size_t i = 0; i < HEIGHT; ++i) {
		for (size_t j = 0; j < WIDTH; ++j) {
			framebuffer[i * WIDTH + j] = 0xff000000;
		}
	}
	hls::stream<command> cmds;
	command cmd;

	cmds << make_cmd_vertex(-0.5,  0.5);
	cmds << make_cmd_vertex(-0.5, -0.5);
	cmds << make_cmd_vertex( 0.5, -0.5);

	cmds << make_cmd_vertex(-0.5,  0.5);
	cmds << make_cmd_vertex( 0.5,  0.5);
	cmds << make_cmd_vertex( 0.5, -0.5);

	cmds << make_cmd_end();

	trinity(cmds, framebuffer);

	cv::Mat mat(HEIGHT, WIDTH, CV_8UC4, framebuffer);
	cv::cvtColor(mat, mat, cv::COLOR_RGBA2BGRA);
	cv::imwrite("image.png", mat);

	return 0;
}
