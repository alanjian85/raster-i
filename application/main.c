#include <stdio.h>
#include <stdbool.h>
#include <SDL2/SDL.h>
#include <unistd.h>

#define WIDTH 1024
#define HEIGHT 720

char framebuffer[HEIGHT * WIDTH * 3];

int main() {
    FILE *eguso = fopen("/dev/eguso", "r");
    SDL_Window *window = SDL_CreateWindow(
        "Eguso",
        SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED,
        WIDTH, HEIGHT, 0
    );
    SDL_Renderer *renderer = SDL_CreateRenderer(window, -1, 0);
    SDL_Texture *texture = SDL_CreateTexture(
        renderer, SDL_PIXELFORMAT_RGB24,
        SDL_TEXTUREACCESS_STREAMING, WIDTH, HEIGHT
    );
    bool quit = false;
    while (!quit) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            switch (event.type) {
                case SDL_QUIT:
                    quit = true;
                    break;
            }
        }
       
      	fread(framebuffer, 3, WIDTH * HEIGHT, eguso);
      	SDL_UpdateTexture(texture, NULL, framebuffer, 3 * WIDTH); 	
        
        SDL_RenderClear(renderer);
        SDL_RenderCopy(renderer, texture, NULL, NULL);
        SDL_RenderPresent(renderer);
    }
    SDL_DestroyTexture(texture);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    fclose(eguso);
    return 0;
}
