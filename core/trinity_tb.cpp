#include <common/xf_headers.hpp>
#include <common/xf_infra.hpp>
#include <stdio.h>
#include "trinity.hpp"

int main(int argc, char **argv) {
	xf::cv::Mat<XF_8UC3, HEIGHT, WIDTH, XF_NPPC1> mat;
	hls::stream<ap_axiu<24, 1, 1, 1>> stream;
	float sine = sin(25.0f), cosine = cos(25.0f);
	trinity(stream, sine, cosine);
	xf::cv::AXIvideo2xfMat(stream, mat);
	xf::cv::imwrite("image.png", mat);
	return 0;
}
