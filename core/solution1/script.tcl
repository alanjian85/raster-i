############################################################
## This file is generated automatically by Vitis HLS.
## Please DO NOT edit it.
## Copyright 1986-2023 Xilinx, Inc. All Rights Reserved.
############################################################
open_project core
set_top trinity
add_files core/vec3.hpp
add_files core/vec2.hpp
add_files core/trinity.hpp
add_files core/trinity.cpp
add_files core/geometry.hpp
add_files -tb core/trinity_tb.cpp -cflags "-I/home/alanjian85/Vitis_Libraries/vision/L1/include/. -I/usr/include/opencv4/. -std=c++0x -Wno-unknown-pragmas" -csimflags "-Wno-unknown-pragmas"
open_solution "solution1" -flow_target vivado
set_part {xck26-sfvc784-2LV-c}
create_clock -period 10 -name default
#source "./core/solution1/directives.tcl"
csim_design -ldflags {-lopencv_core -lopencv_imgcodecs -lopencv_imgproc}
csynth_design
cosim_design -ldflags {-lopencv_core -lopencv_imgcodecs -lopencv_imgproc}
export_design -rtl verilog -format ip_catalog
