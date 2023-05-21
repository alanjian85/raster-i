// ==============================================================
// Vitis HLS - High-Level Synthesis from C, C++ and OpenCL v2022.2.2 (64-bit)
// Tool Version Limit: 2019.12
// Copyright 1986-2023 Xilinx, Inc. All Rights Reserved.
// ==============================================================
/***************************** Include Files *********************************/
#include "xtrinity.h"

/************************** Function Implementation *************************/
#ifndef __linux__
int XTrinity_CfgInitialize(XTrinity *InstancePtr, XTrinity_Config *ConfigPtr) {
    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(ConfigPtr != NULL);

    InstancePtr->Control_BaseAddress = ConfigPtr->Control_BaseAddress;
    InstancePtr->IsReady = XIL_COMPONENT_IS_READY;

    return XST_SUCCESS;
}
#endif

void XTrinity_Set_m_axi_mm_video(XTrinity *InstancePtr, u64 Data) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_M_AXI_MM_VIDEO_DATA, (u32)(Data));
    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_M_AXI_MM_VIDEO_DATA + 4, (u32)(Data >> 32));
}

u64 XTrinity_Get_m_axi_mm_video(XTrinity *InstancePtr) {
    u64 Data;

    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_M_AXI_MM_VIDEO_DATA);
    Data += (u64)XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_M_AXI_MM_VIDEO_DATA + 4) << 32;
    return Data;
}

