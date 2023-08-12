#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

int main() {
    printf("memory_initialization_radix=16;\n");
    printf("memory_initialization_vector=");
    for (int i = 0; i < 360; ++i) { 
        double cosine = cos(i * M_PI / 180);
        int icosine = cosine * 1024;
        if (abs(icosine) >= 1024)
            icosine = icosine > 0 ? 0x3ff : -0x3ff;
        printf("%03x", (icosine & 0x3ff) | ((icosine < 0) << 10));

        if (i != 359)
            printf(" ");
    }
    printf(";\n");
    return 0;
}
