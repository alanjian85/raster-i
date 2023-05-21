#ifndef TRINITY_HPP
#define TRINITY_HPP

#include <cstdint>

#include <hls_stream.h>
#include <ap_axi_sdata.h>

#include "vec2.hpp"
#include "vec3.hpp"
#include "geometry.hpp"
#include "utils.hpp"

#define WIDTH 1920
#define HEIGHT 1080

enum {
	CMD_VERTEX = 0x00,
	CMD_END  = 0x01
};

using command = ap_axiu<128, 0, 0, 0>;

inline command make_cmd_vertex(float x, float y) {
	command cmd;
	cmd.data = CMD_VERTEX | static_cast<ap_uint<128>>(float2bits(x)) << 32 | static_cast<ap_uint<128>>(float2bits(y)) << 64;
	return cmd;
}

inline command make_cmd_end() {
	command cmd;
	cmd.data = CMD_END;
	return cmd;
}

void trinity(hls::stream<command>& s_axis_cmds, volatile uint32_t* m_axi_mm_video);

#endif // TRINITY_HPP
