#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

int main() {
    printf("memory_initialization_radix=16;\n");
    printf("memory_initialization_vector=");
    for (int i = 0; i < 360; ++i) { 
        double bz = 1 / (2 - sin(i * M_PI / 180));
        int ibz = bz * 1024;
        printf("%02x", (ibz >> 3) & 0x7F);
        if (i != 359)
            printf(" ");
    }
    printf(";\n");
    return 0;
}
