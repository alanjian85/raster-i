#ifndef TRINITY_HPP
#define TRINITY_HPP

#include <hls_stream.h>

#include "vec2.hpp"
#include "vec3.hpp"
#include "geometry.hpp"

#define WIDTH 1920
#define HEIGHT 1080

enum {
	CMD_VERTEX = 0x00,
	CMD_END  = 0x01
};

struct command {
	uint8_t cmd;
	union {
		vec2 vertex;
	};
};

void trinity(hls::stream<command>& s_axis_command, volatile uint32_t* m_axi_mm_video);

#endif // TRINITY_HPP
