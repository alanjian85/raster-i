// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

#include <memory>
#include <iostream>
#include <SDL2/SDL.h>
#include "VTrinitySdl.h"

uint32_t filter(uint32_t x) {
    return (x & 0x000000f0) << 24 |
           (x & 0x0000f000) << 8  |
           (x & 0x00f00000) >> 8  |
           (x & 0xf0000000) >> 24;
}

int main() {
    auto trinity = std::make_unique<VTrinitySdl>();

    SDL_Init(SDL_INIT_VIDEO);
    SDL_Window *window = SDL_CreateWindow(
            "Project Trinity",
            SDL_WINDOWPOS_UNDEFINED,
            SDL_WINDOWPOS_UNDEFINED,
            1024,
            768,
            0
            );
    SDL_Renderer *renderer = SDL_CreateRenderer(
            window,
            -1,
            0
            );
    SDL_Texture *texture = SDL_CreateTexture(
            renderer,
            SDL_PIXELFORMAT_RGBA8888,
            SDL_TEXTUREACCESS_STREAMING,
            1024,
            768
            );
    uint32_t framebuffer[1024 * 768];

    trinity->reset = 1;
    trinity->clock = 0;
    trinity->io_angle = 0;
    trinity->io_x = 0;
    trinity->io_y = 0;
    trinity->eval();
    trinity->clock = 1;
    trinity->eval();
    trinity->reset = 0;
    trinity->clock = 0;
    trinity->eval();

    for (int i = 0; i < 15; ++i) {
        trinity->clock = 1;
        trinity->eval();
        trinity->clock = 0;
        trinity->eval();
        trinity->io_x++;
    }

    bool quit = false;
    int x = 0, y = 0;
    while (!quit) {
        trinity->clock = 1;
        trinity->eval();
        trinity->clock = 0;
        trinity->eval();
        std::cout << "X: " << int(trinity->io_outX) << " Y: " << int(trinity->io_outY) << '\n'; 
        framebuffer[y * 1024 + x * 4 + 0] = filter(trinity->io_pix_0);
        framebuffer[y * 1024 + x * 4 + 1] = filter(trinity->io_pix_1);
        framebuffer[y * 1024 + x * 4 + 2] = filter(trinity->io_pix_2);
        framebuffer[y * 1024 + x * 4 + 3] = filter(trinity->io_pix_3);
        if (trinity->io_x++ == 255) {
            trinity->io_x = 0;
            if (trinity->io_y++ == 767)
                trinity->io_y = 0;
        }
        if (x++ == 255) {
            x = 0;
            if (y++ == 767) {
                y = 0;
                float mul = SDL_GetTicks() / (13.0f * 360);
                trinity->io_angle = (mul - (int) mul) * 360;
            } else {
                continue;
            }
        } else {
            continue;
        }

        SDL_Event event;
        SDL_PollEvent(&event);
        if (event.type == SDL_QUIT) {
            quit = true;
            continue;
        }

        SDL_UpdateTexture(texture, nullptr, framebuffer, 1024 * 4);
        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 0);
        SDL_RenderClear(renderer);
        SDL_RenderCopy(renderer, texture, nullptr, nullptr);
        SDL_RenderPresent(renderer);
    }

    SDL_DestroyTexture(texture);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}
