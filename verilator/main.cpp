// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

#include <memory>
#include <SDL2/SDL.h>
#include "VTrinitySdl.h"

int main() {
    auto trinity = std::make_unique<VTrinitySdl>();

    SDL_Init(SDL_INIT_VIDEO);
    SDL_Window *window = SDL_CreateWindow(
        "Project Trinity",
        SDL_WINDOWPOS_UNDEFINED,
        SDL_WINDOWPOS_UNDEFINED,
        800,
        600,
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
        800,
        600
    );
    uint32_t framebuffer[800 * 600];

    trinity->reset = 1;
    trinity->clock = 0;
    trinity->eval();
    trinity->clock = 1;
    trinity->eval();
    trinity->reset = 0;
    trinity->clock = 0;
    trinity->eval();

    bool quit = false;
    while (!quit) {
        trinity->clock = 1;
        trinity->eval();
        trinity->clock = 0;
        trinity->eval();

        if (trinity->io_active) {
            uint8_t r = trinity->io_pix_r | trinity->io_pix_r << 4;
            uint8_t g = trinity->io_pix_g | trinity->io_pix_g << 4;
            uint8_t b = trinity->io_pix_b | trinity->io_pix_b << 4;
            framebuffer[trinity->io_pos_y * 800 + trinity->io_pos_x] = r << 24 | g << 16 | b << 8 | 0xff;
        }

        if (trinity->io_pos_x == 0 && trinity->io_pos_y == 600) {
            SDL_Event event;
            SDL_PollEvent(&event);
            if (event.type == SDL_QUIT) {
                quit = true;
                continue;
            }

            SDL_UpdateTexture(texture, nullptr, framebuffer, 800 * 4);
            SDL_SetRenderDrawColor(renderer, 0, 0, 0, 0);
            SDL_RenderClear(renderer);
            SDL_RenderCopy(renderer, texture, nullptr, nullptr);
            SDL_RenderPresent(renderer);
        }
    }

    SDL_DestroyTexture(texture);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}
