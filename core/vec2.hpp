#ifndef VEC2_HPP
#define VEC2_HPP

struct vec2 {
	vec2() = default;

	vec2(float f) {
		x = f;
		y = f;
	}

	vec2(float x, float y) {
		this->x = x;
		this->y = y;
	}

	float x;
	float y;
};

inline vec2 operator+(vec2 lhs, vec2 rhs) {
	return vec2(lhs.x + rhs.x, lhs.y + rhs.y);
}

inline vec2 operator-(vec2 lhs, vec2 rhs) {
	return vec2(lhs.x - rhs.x, lhs.y - rhs.y);
}

inline vec2 operator*(float lhs, vec2 rhs) {
	return vec2(lhs * rhs.x, lhs * rhs.y);
}

#endif // VEC2_HPP
