#ifndef GEOMETRY_HPP
#define GEOMETRY_HPP

#include "vec2.hpp"
#include "vec3.hpp"

inline vec3 barycentric(vec2 a, vec2 b, vec2 c, vec2 p) {
	vec2 ab = b - a, ac = c - a, pa = a - p;
	vec3 x(ab.x, ac.x, pa.x),
		 y(ab.y, ac.y, pa.y);
	auto uv = cross(x, y);
	return vec3(1 - uv.x / uv.z - uv.y / uv.z, uv.x / uv.z, uv.y / uv.z);
}

#endif // GEOMETRY_HPP
