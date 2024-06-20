#include <SDL2/SDL_hints.h>
#include <SDL2/SDL_render.h>
#include <SDL2/SDL_timer.h>
#include <cstddef>
#include <string>

#include <SDL2/SDL.h>

#include <fb.hpp>

void trinity_renderer(fb_id_t fb_id, uint32_t *vram, ap_uint<9> angle);

static uint32_t vram[4 * 1024 * 1024];

int main() {
    SDL_Init(SDL_INIT_VIDEO);

    SDL_Window *window =
        SDL_CreateWindow("Trinitas", SDL_WINDOWPOS_UNDEFINED,
                         SDL_WINDOWPOS_UNDEFINED, FB_WIDTH, FB_HEIGHT, 0);

    SDL_Renderer *renderer =
        SDL_CreateRenderer(window, -1, SDL_RENDERER_PRESENTVSYNC);

    SDL_Texture *texture =
        SDL_CreateTexture(renderer, SDL_PIXELFORMAT_ABGR8888,
                          SDL_TEXTUREACCESS_STREAMING, FB_WIDTH, FB_HEIGHT);

    bool quit = false;
    int fb_id = 0;
    while (!quit) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            switch (event.type) {
            case SDL_QUIT:
                quit = true;
                break;
            }
        }

        SDL_SetRenderDrawColor(renderer, 0xFF, 0x00, 0xFF, 0xFF);
        SDL_RenderClear(renderer);

        Uint64 start = SDL_GetPerformanceCounter();
        trinity_renderer(fb_id, vram, (SDL_GetTicks() / 14) % 360);
        Uint64 end = SDL_GetPerformanceCounter();
        float fps =
            SDL_GetPerformanceFrequency() / static_cast<float>(end - start);
        SDL_SetWindowTitle(window,
                           ("Trinitas, FPS: " + std::to_string(fps)).c_str());

        SDL_UpdateTexture(texture, nullptr, vram + ((fb_id - 1) % 1 << 20),
                          FB_WIDTH * 4);
        SDL_RenderCopy(renderer, texture, nullptr, nullptr);

        SDL_RenderPresent(renderer);

        fb_id = (fb_id + 1) % 1;
    }

    SDL_DestroyTexture(texture);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
}
