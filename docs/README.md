<img src="logo.svg" align="right" width="125" height="125"/>

# Raster I
Raster I, also known as Raster Primus, is a hardware renderer specialized in real-time rasterization. While Chisel HDL (Scala) is used to precisely describe the logic of the VGA controller, framebuffer reader, and Vsync mechanisms. The primary graphics pipeline, which contributes to the majority of computations, is implemented in Vitis HLS (C++) to enhance productivity.

Currently, a [Pineda](https://www.cs.drexel.edu/~deb39/Classes/Papers/comp175-06-pineda.pdf) style rasterizer is implemented with modern techniques such as tiled rendering and tile-based deferred rendering (TBDR). The resolution can be configured up to 1024x768 (each scanline thus represents the maximum amount of data that an AXI write burst can send), and tiles of size 64x32 are rendered successively. Furthermore, 8 parallel pipelines are in charge of interpolating pixel attributes in each tile, while another hardware pipeline that implements a deferred shader shades the surface using the Phong shading model and determines the color of each pixel based on the interpolated data. Finally, ordered dithering is supported for transferring 24 bpp frames via a 12 bpp VGA adaptor, and anti-aliasing is achieved using MSAA 4x with almost no overhead.

In conclusion, implementing the following features result in an efficient GPU that is capable of rendering a reduced [Stanford Lucy](https://github.com/alecjacobson/common-3d-test-models/blob/master/data/lucy.obj) model, composed of 1501 vertices and 2998 triangles, at nearly 30 FPS.

* Incremental version of Pineda's parallel algorithm for rasterization
* Interpolation of pixel attributes based on barycentric coordinates
* Tiled rendering and tile-based deferred rendering (TBDR)
* Phong shading and Lambertian reflectance
* Back-face culling for skipping invisible primitives
* Ordered dithering converting RGB888 to RGB444
* Vsync making the render passes synchronous with the VGA signal
* MSAA 4x with very low performance overhead (thanks to tiled rendering)

|<img src="demo1.gif" width="180" height="300"/>|<img src="demo2.gif" width="300" height="300"/>|
|-----------------------------------------------|-----------------------------------------------|
|<img src="demo3.gif" width="300" height="300"/>|<img src="demo4.gif" width="300" height="300"/>|

Note also that this project is just the first iteration of Project Raster, which aims to provide a full-featured, open-source GPU suitable for deployment on widely available FPGA platforms. The ultimate goal is to incorporate a 3D graphics engine and a programmable shader pipeline into the architecture, resulting in an IP that is compatible with modern graphics APIs like OpenGL 4 and Vulkan (which is uncommon in current open-source GPUs). While currently in the experimental phase, ongoing development is expected to progress in the coming years, potentially resulting in a fully functional and high-performance GPU implemented entirely in HDL and using only open-source toolchains.

## Build Instructions
To build Raster I, three components must be built sequentially: Chisel HDL modules, the HLS graphics pipeline and eventually Raster I itself. All the Chisel HDL source code is placed at `system/src/main/scala`. Therefore, you need to first go to the `system` directory and execute the following command. 
```shell
sbt run
```

After Scala finishes executing, a SystemVerilog file, which defines the structure of the whole system and several crucial modules, will be available under `system/generated`. Then, run Vitis IDE and open the directory of this project as workspace. You will see a HLS component available in the index, which is the main graphics pipeline and renderer. Click `Synthesis` and `Package` to generate HDL files from HLS source code.

Now open Vivado at `system/vivado` and run `build.tcl` (`Tools` -> `Run Tcl Script`). If done successfully, the project will be initialized with all source files included and Arty A7-100T set as the FPGA board (the board package must be installed in advance). Note that the IP version of the HLS kernel referenced in the Vivado project must be updated, which can be done by refreshing the IP Catalog and clicking `Upgrade Selected` in IP Status.

After clicking `Generate Bitstream`, it will take several minutes (up to the speed of your computer) to generate the FPGA bitstream. If no error is generated, you can now open hardware manager and program the device.
