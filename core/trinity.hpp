#ifndef TRINITY_HPP
#define TRINITY_HPP

#include "vec2.hpp"
#include "vec3.hpp"
#include "geometry.hpp"

#define WIDTH 1920
#define HEIGHT 1080

void trinity(hls::stream<ap_axiu<24, 1, 1, 1>>& m_axis_video, float sine);

#endif // TRINITY_HPP
