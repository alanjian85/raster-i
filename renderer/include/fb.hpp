#pragma once

#include <ap_int.h>
#include <cstdint>

#include <math/vec.hpp>

#define FB_WIDTH 1024
#define FB_HEIGHT 768
#define FB_ID_SHIFT 20

using fb_id_t = ap_uint<1>;