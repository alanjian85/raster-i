#ifndef UTILS_HPP
#define UTILS_HPP

#include <cstdint>

inline float bits2float(uint32_t x) {
	return *reinterpret_cast<float*>(&x);
}

inline uint32_t float2bits(float x) {
	return *reinterpret_cast<uint32_t*>(&x);
}

#endif // UTILS_HPP
