#ifndef VEC3_HPP
#define VEC3_HPP

struct vec3 {
	vec3(float x, float y, float z) {
		this->x = x;
		this->y = y;
		this->z = z;
	}

	float x;
	float y;
	float z;
};

inline vec3 cross(vec3 lhs, vec3 rhs) {
	return vec3(
		lhs.y * rhs.z - lhs.z * rhs.y,
		lhs.z * rhs.x - lhs.x * rhs.z,
		lhs.x * rhs.y - lhs.y * rhs.x
	);
}

#endif // VEC3_HPP
