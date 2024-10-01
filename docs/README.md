<img src="logo.svg" align="right" width="125" height="125"/>

# Raster I
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/license/mit)

Raster I is a hardware renderer that specializes in real-time rasterization and is based on the [Tile-Based Deferred Rendering (TBDR)](https://en.wikipedia.org/wiki/Tiled_rendering) architecture. Currently, several crucial features are implemented along with a tiled [Pineda](https://www.cs.drexel.edu/~deb39/Classes/Papers/comp175-06-pineda.pdf) style rasterizer, including hardware-accelerated transform and lighting (T&L), deferred Phong shading, double buffering, VSync, MSAA anti-aliasing, ordered dithering and back-face culling. Its implementation is divided into two parts, one is written in [Chisel HDL](https://www.chisel-lang.org/), and the other is based on [Xilinx Vitis HLS](https://www.amd.com/en/products/software/adaptive-socs-and-fpgas/vitis/vitis-hls.html). 

Furthermore, Raster I consists of a multi-cycle vertex transformer, 8 parallel interpolator pipelines, and a deferred shading pipeline that employs the Phong shading model (internal calculations use Q11.13 fixed point numbers). The output VGA signal can be configured up to 1024x768 @ 60Hz, and tiles of size 64x32 are rendered sequentially. Visual enhancements are also supported with minimal overhead, such as ordered dithering for displaying pseudo 24bpp pixels and MSAA 4x anti-aliasing. If there is enough BRAM left over for texture storage, an optional texture sampling unit is also available.

As a result, this GPU utilizes 69% LUT, 97% BRAM, and 88% DSP from [Digilent Arty A7-100T](https://digilent.com/shop/arty-a7-100t-artix-7-fpga-development-board/) and can render a 3D model with 3K faces at a screen resolution of 1024x768 and a clock frequency of 100MHz at about 30FPS. It is also worth mentioning that this is only the first iteration of Project Raster, with key features like GPGPU ISA yet to be implemented. Therefore, in future releases, it will eventually evolve into a fully-fledged open-source hardware that supports practically all of the typical features of modern GPUs.

|<img src="demo1.gif" width="180" height="300"/>|<img src="demo2.gif" width="300" height="300"/>|
|-----------------------------------------------|-----------------------------------------------|
|<img src="demo3.gif" width="300" height="300"/>|<img src="demo4.gif" width="300" height="300"/>|

## Architecture

![System Architecture](system-architecture.png)
![Rendering Unit Architecture](rendering-unit.png)

The architecture of Raster I can be mainly viewed as 3 clock domains: system, graphics and display (their frequencies are currently 100MHz, 100MHz and 65MHz). While the components in the graphics clock domain is responsible for executing traditional rendering processes, the display clock domain is in charge of reading the framebuffer in DRAM, applying effects like dithering and putting it onto the screen synchronously. At the heart of the system, there sits a framebuffer swapper which behaves as a coordinator between the graphics and display clock domains.
