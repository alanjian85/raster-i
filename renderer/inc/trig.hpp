#pragma once

struct Triangle {
    int x[3];
    int y[3];
    float z[3];
    float u[3];
    float v[3];

    int area() const {
        return abs((x[1] - x[0]) * (y[2] - y[0]) - (y[1] - y[0]) * (x[2] - x[0]));
    }

    std::tuple<bool, int, int, int> barycentric(int px, int py) const {
        int d1 = (x[1] - x[0]) * (py - y[0]) - (y[1] - y[0]) * (px - x[0]);
        int d2 = (x[2] - x[1]) * (py - y[1]) - (y[2] - y[1]) * (px - x[1]);
        int d3 = (x[0] - x[2]) * (py - y[2]) - (y[0] - y[2]) * (px - x[2]);

        bool visible;
        if (d1 <= 0) {
            visible = d2 <= 0 && d3 <= 0;
            d1 = -d1;
            d2 = -d2;
            d3 = -d3;
        } else {
            visible = d2 > 0 && d3 > 0;            
        }

        return std::make_tuple(visible, d2, d3, d1);
    }
};

struct FTriangle {
    float x[3];
    float y[3];
    float z[3];
    float u[3];
    float v[3];
};

void build_triangles(FTriangle *ftriangles) {
    ftriangles[0].x[0] = -0.5;
    ftriangles[0].y[0] = -0.5;
    ftriangles[0].z[0] = -0.500001;
    ftriangles[0].u[0] = 0.0;
    ftriangles[0].v[0] = 0.0;
    ftriangles[0].x[1] = -0.5;
    ftriangles[0].y[1] = 0.5;
    ftriangles[0].z[1] = -0.500001;
    ftriangles[0].u[1] = 0.0;
    ftriangles[0].v[1] = 1.0;
    ftriangles[0].x[2] = 0.5;
    ftriangles[0].y[2] = -0.5;
    ftriangles[0].z[2] = -0.500001;
    ftriangles[0].u[2] = 1.0;
    ftriangles[0].v[2] = 0.0;
    
    ftriangles[1].x[0] = -0.5;
    ftriangles[1].y[0] = 0.5;
    ftriangles[1].z[0] = -0.500001;
    ftriangles[1].u[0] = 0.0;
    ftriangles[1].v[0] = 1.0;
    ftriangles[1].x[1] = 0.5;
    ftriangles[1].y[1] = -0.5;
    ftriangles[1].z[1] = -0.500001;
    ftriangles[1].u[1] = 1.0;
    ftriangles[1].v[1] = 0.0;
    ftriangles[1].x[2] = 0.5;
    ftriangles[1].y[2] = 0.5;
    ftriangles[1].z[2] = -0.500001;
    ftriangles[1].u[2] = 1.0;
    ftriangles[1].v[2] = 1.0;

    ftriangles[2].x[0] = -0.5;
    ftriangles[2].y[0] = -0.5;
    ftriangles[2].z[0] = 0.500001;
    ftriangles[2].u[0] = 0.0;
    ftriangles[2].v[0] = 0.0;
    ftriangles[2].x[1] = -0.5;
    ftriangles[2].y[1] = 0.5;
    ftriangles[2].z[1] = 0.500001;
    ftriangles[2].u[1] = 0.0;
    ftriangles[2].v[1] = 1.0;
    ftriangles[2].x[2] = 0.5;
    ftriangles[2].y[2] = -0.5;
    ftriangles[2].z[2] = 0.500001;
    ftriangles[2].u[2] = 1.0;
    ftriangles[2].v[2] = 0.0;

    ftriangles[3].x[0] = -0.5;
    ftriangles[3].y[0] = 0.5;
    ftriangles[3].z[0] = 0.500001;
    ftriangles[3].u[0] = 0.0;
    ftriangles[3].v[0] = 1.0;
    ftriangles[3].x[1] = 0.5;
    ftriangles[3].y[1] = -0.5;
    ftriangles[3].z[1] = 0.500001;
    ftriangles[3].u[1] = 1.0;
    ftriangles[3].v[1] = 0.0;
    ftriangles[3].x[2] = 0.5;
    ftriangles[3].y[2] = 0.5;
    ftriangles[3].z[2] = 0.500001;
    ftriangles[3].u[2] = 1.0;
    ftriangles[3].v[2] = 1.0;

    ftriangles[4].x[0] = -0.500001;
    ftriangles[4].y[0] = -0.5;
    ftriangles[4].z[0] = -0.5;
    ftriangles[4].u[0] = 1.0;
    ftriangles[4].v[0] = 0.0;
    ftriangles[4].x[1] = -0.500001;
    ftriangles[4].y[1] = 0.5;
    ftriangles[4].z[1] = -0.5;
    ftriangles[4].u[1] = 1.0;
    ftriangles[4].v[1] = 1.0;
    ftriangles[4].x[2] = -0.500001;
    ftriangles[4].y[2] = -0.5;
    ftriangles[4].z[2] = 0.5;
    ftriangles[4].u[2] = 0.0;
    ftriangles[4].v[2] = 0.0;

    ftriangles[5].x[0] = -0.500001;
    ftriangles[5].y[0] = 0.5;
    ftriangles[5].z[0] = -0.5;
    ftriangles[5].u[0] = 1.0;
    ftriangles[5].v[0] = 1.0;
    ftriangles[5].x[1] = -0.500001;
    ftriangles[5].y[1] = -0.5;
    ftriangles[5].z[1] = 0.5;
    ftriangles[5].u[1] = 0.0;
    ftriangles[5].v[1] = 0.0;
    ftriangles[5].x[2] = -0.500001;
    ftriangles[5].y[2] = 0.5;
    ftriangles[5].z[2] = 0.5;
    ftriangles[5].u[2] = 0.0;
    ftriangles[5].v[2] = 1.0;

    ftriangles[6].x[0] = 0.500001;
    ftriangles[6].y[0] = -0.5;
    ftriangles[6].z[0] = -0.5;
    ftriangles[6].u[0] = 1.0;
    ftriangles[6].v[0] = 0.0;
    ftriangles[6].x[1] = 0.500001;
    ftriangles[6].y[1] = 0.5;
    ftriangles[6].z[1] = -0.5;
    ftriangles[6].u[1] = 1.0;
    ftriangles[6].v[1] = 1.0;
    ftriangles[6].x[2] = 0.500001;
    ftriangles[6].y[2] = -0.5;
    ftriangles[6].z[2] = 0.5;
    ftriangles[6].u[2] = 0.0;
    ftriangles[6].v[2] = 0.0;

    ftriangles[7].x[0] = 0.500001;
    ftriangles[7].y[0] = 0.5;
    ftriangles[7].z[0] = -0.5;
    ftriangles[7].u[0] = 1.0;
    ftriangles[7].v[0] = 1.0;
    ftriangles[7].x[1] = 0.500001;
    ftriangles[7].y[1] = -0.5;
    ftriangles[7].z[1] = 0.5;
    ftriangles[7].u[1] = 0.0;
    ftriangles[7].v[1] = 0.0;
    ftriangles[7].x[2] = 0.500001;
    ftriangles[7].y[2] = 0.5;
    ftriangles[7].z[2] = 0.5;
    ftriangles[7].u[2] = 0.0;
    ftriangles[7].v[2] = 1.0;

    ftriangles[8].x[0] = -0.5;
    ftriangles[8].y[0] = -0.500001;
    ftriangles[8].z[0] = -0.5;
    ftriangles[8].u[0] = 0.0;
    ftriangles[8].v[0] = 0.0;
    ftriangles[8].x[1] = 0.5;
    ftriangles[8].y[1] = -0.500001;
    ftriangles[8].z[1] = -0.5;
    ftriangles[8].u[1] = 1.0;
    ftriangles[8].v[1] = 0.0;
    ftriangles[8].x[2] = 0.5;
    ftriangles[8].y[2] = -0.500001;
    ftriangles[8].z[2] = 0.5;
    ftriangles[8].u[2] = 1.0;
    ftriangles[8].v[2] = 1.0;

    ftriangles[9].x[0] = -0.5;
    ftriangles[9].y[0] = -0.500001;
    ftriangles[9].z[0] = -0.5;
    ftriangles[9].u[0] = 0.0;
    ftriangles[9].v[0] = 0.0;
    ftriangles[9].x[1] = 0.5;
    ftriangles[9].y[1] = -0.500001;
    ftriangles[9].z[1] = 0.5;
    ftriangles[9].u[1] = 1.0;
    ftriangles[9].v[1] = 1.0;
    ftriangles[9].x[2] = -0.5;
    ftriangles[9].y[2] = -0.500001;
    ftriangles[9].z[2] = 0.5;
    ftriangles[9].u[2] = 0.0;
    ftriangles[9].v[2] = 1.0;

    ftriangles[10].x[0] = -0.5;
    ftriangles[10].y[0] = 0.500001;
    ftriangles[10].z[0] = -0.5;
    ftriangles[10].u[0] = 0.0;
    ftriangles[10].v[0] = 0.0;
    ftriangles[10].x[1] = 0.5;
    ftriangles[10].y[1] = 0.500001;
    ftriangles[10].z[1] = -0.5;
    ftriangles[10].u[1] = 1.0;
    ftriangles[10].v[1] = 0.0;
    ftriangles[10].x[2] = 0.5;
    ftriangles[10].y[2] = 0.500001;
    ftriangles[10].z[2] = 0.5;
    ftriangles[10].u[2] = 1.0;
    ftriangles[10].v[2] = 1.0;

    ftriangles[11].x[0] = -0.5;
    ftriangles[11].y[0] = 0.500001;
    ftriangles[11].z[0] = -0.5;
    ftriangles[11].u[0] = 0.0;
    ftriangles[11].v[0] = 0.0;
    ftriangles[11].x[1] = 0.5;
    ftriangles[11].y[1] = 0.500001;
    ftriangles[11].z[1] = 0.5;
    ftriangles[11].u[1] = 1.0;
    ftriangles[11].v[1] = 1.0;
    ftriangles[11].x[2] = -0.5;
    ftriangles[11].y[2] = 0.500001;
    ftriangles[11].z[2] = 0.5;
    ftriangles[11].u[2] = 0.0;
    ftriangles[11].v[2] = 1.0;
}