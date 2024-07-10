#pragma once

template <typename T> struct Vec2 {
    T x, y;

    Vec2() = default;

    Vec2(T x, T y) {
        this->x = x;
        this->y = y;
    }
};

using Vec2i = Vec2<int>;
using Vec2f = Vec2<float>;

template <typename T>
Vec2<T> operator+(const Vec2<T> &lhs, const Vec2<T> &rhs) {
    return Vec2<T>(lhs.x + rhs.x, lhs.y + rhs.y);
}

template <typename T> Vec2<T> operator*(const Vec2<T> &lhs, T rhs) {
    return Vec2<T>(lhs.x * rhs, lhs.y * rhs);
}

template <typename T> Vec2<T> operator*(T lhs, const Vec2<T> &rhs) {
    return Vec2<T>(lhs * rhs.x, lhs * rhs.y);
}

template <typename T> struct Vec3 {
    T x, y, z;

    Vec3() = default;

    Vec3(T x, T y, T z) {
        this->x = x;
        this->y = y;
        this->z = z;
    }

    template <typename U> Vec3(const Vec3<U> other) {
        this->x = other.x;
        this->y = other.y;
        this->z = other.z;
    }

    Vec3 operator-() const { return Vec3(-x, -y, -z); }

    template <typename U> Vec3 &operator/=(U rhs) {
        x /= rhs;
        y /= rhs;
        z /= rhs;
        return *this;
    }
};

template <typename T>
Vec3<T> operator+(const Vec3<T> &lhs, const Vec3<T> &rhs) {
    return Vec3<T>(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z);
}

template <typename T>
Vec3<T> operator-(const Vec3<T> &lhs, const Vec3<T> &rhs) {
    return Vec3<T>(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z);
}

template <typename T, typename U> Vec3<T> operator*(const Vec3<T> &lhs, U rhs) {
    return Vec3<T>(lhs.x * rhs, lhs.y * rhs, lhs.z * rhs);
}

template <typename T> Vec3<T> operator*(T lhs, const Vec3<T> &rhs) {
    return Vec3<T>(lhs * rhs.x, lhs * rhs.y, lhs * rhs.z);
}

template <typename T> Vec3<T> operator/(const Vec3<T> &lhs, T rhs) {
    return Vec3<T>(lhs.x / rhs, lhs.y / rhs, lhs.z / rhs);
}

template <typename T> T dot(const Vec3<T> &lhs, const Vec3<T> &rhs) {
    return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z;
}

template <typename T> Vec3<T> cross(const Vec3<T> &lhs, const Vec3<T> &rhs) {
    return Vec3<T>(lhs.y * rhs.z - lhs.z * rhs.y, lhs.z * rhs.x - lhs.x * rhs.z,
                   lhs.x * rhs.y - lhs.y * rhs.x);
}

using Vec3i = Vec3<int>;
using Vec3f = Vec3<float>;
