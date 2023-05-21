// ==============================================================
// Vitis HLS - High-Level Synthesis from C, C++ and OpenCL v2022.2.2 (64-bit)
// Tool Version Limit: 2019.12
// Copyright 1986-2023 Xilinx, Inc. All Rights Reserved.
// ==============================================================
// control
// 0x00 : reserved
// 0x04 : reserved
// 0x08 : reserved
// 0x0c : reserved
// 0x10 : Data signal of m_axi_mm_video
//        bit 31~0 - m_axi_mm_video[31:0] (Read/Write)
// 0x14 : Data signal of m_axi_mm_video
//        bit 31~0 - m_axi_mm_video[63:32] (Read/Write)
// 0x18 : reserved
// (SC = Self Clear, COR = Clear on Read, TOW = Toggle on Write, COH = Clear on Handshake)

#define XTRINITY_CONTROL_ADDR_M_AXI_MM_VIDEO_DATA 0x10
#define XTRINITY_CONTROL_BITS_M_AXI_MM_VIDEO_DATA 64

