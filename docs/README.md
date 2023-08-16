<img src="logo.svg" align="right" width="125" height="125"/>

# Project Trinity
A rasterizer-based GPU for real-time rendering

## Overview
The project Trinity, as its name implies, is not only an open-source hardware implementation of a [graphics processing unit (GPU)](https://en.wikipedia.org/wiki/Graphics_processing_unit). In essence, It consists of a GPU hardware together with its driver and architecture (and tools targeting the architecture). They are all a piece of Trinity.

The motivation is to build a full-featured and open-source GPU that can runs on common FPGA platforms. Presently, It is primarily written in [Scala](https://scala-lang.org/) and [Chisel](https://www.chisel-lang.org/), an open-source and relatively high-level (in comparison with Verilog, SystemVerilog and VHDL) hardware description language (HDL) that can be used to describe combinational and synchronous circuits. 

On the contrary with other open-source GPUs, the ambition of this project is to build a hardware that supports both 3D real-time rendering and programmable pipeline (in the form of [shader](https://en.wikipedia.org/wiki/Shader), a program that models the shading of objects with its parallel nature) at the same time. And the ultimate goal is to support modern graphics APIs such as [OpenGL](https://www.opengl.org/) 4 and [Vulkan](https://www.vulkan.org/).

## Build instructions

To build and run this project on an [AMD Xilinx](https://www.amd.com/en/products/xilinx) FPGA board, the first step is to follow the [Chisel setup manual](https://github.com/chipsalliance/chisel/blob/main/SETUP.md) and install the required tools. Then you can clone the GitHub repository and compile the Chisel code in the project directory:

```
git clone https://github.com/alanjian85/trinity && cd trinity
sbt run
```

There should now be some SystemVerilog files in the `generated` directory. And the third step is to run Vivado in the `vivado` directory and execute the TCL script `build.tcl`. If you click the `Generate Bitstream` button now, you will get a file that can be loaded into FPGA. Have fun!

## License (MIT)
<a href="https://opensource.org/licenses/MIT" target="_blank">
<img align="right" src="osi.png">
</a>

```
MIT License

Copyright (c) 2023 Alan Jian

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
```