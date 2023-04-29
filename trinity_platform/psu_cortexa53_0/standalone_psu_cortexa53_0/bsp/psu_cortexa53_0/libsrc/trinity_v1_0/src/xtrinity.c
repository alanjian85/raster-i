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

void XTrinity_Start(XTrinity *InstancePtr) {
    u32 Data;

    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL) & 0x80;
    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL, Data | 0x01);
}

u32 XTrinity_IsDone(XTrinity *InstancePtr) {
    u32 Data;

    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL);
    return (Data >> 1) & 0x1;
}

u32 XTrinity_IsIdle(XTrinity *InstancePtr) {
    u32 Data;

    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL);
    return (Data >> 2) & 0x1;
}

u32 XTrinity_IsReady(XTrinity *InstancePtr) {
    u32 Data;

    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL);
    // check ap_start to see if the pcore is ready for next input
    return !(Data & 0x1);
}

void XTrinity_EnableAutoRestart(XTrinity *InstancePtr) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL, 0x80);
}

void XTrinity_DisableAutoRestart(XTrinity *InstancePtr) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_AP_CTRL, 0);
}

void XTrinity_Set_sine(XTrinity *InstancePtr, u32 Data) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_SINE_DATA, Data);
}

u32 XTrinity_Get_sine(XTrinity *InstancePtr) {
    u32 Data;

    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Data = XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_SINE_DATA);
    return Data;
}

void XTrinity_InterruptGlobalEnable(XTrinity *InstancePtr) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_GIE, 1);
}

void XTrinity_InterruptGlobalDisable(XTrinity *InstancePtr) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_GIE, 0);
}

void XTrinity_InterruptEnable(XTrinity *InstancePtr, u32 Mask) {
    u32 Register;

    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Register =  XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_IER);
    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_IER, Register | Mask);
}

void XTrinity_InterruptDisable(XTrinity *InstancePtr, u32 Mask) {
    u32 Register;

    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    Register =  XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_IER);
    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_IER, Register & (~Mask));
}

void XTrinity_InterruptClear(XTrinity *InstancePtr, u32 Mask) {
    Xil_AssertVoid(InstancePtr != NULL);
    Xil_AssertVoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    XTrinity_WriteReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_ISR, Mask);
}

u32 XTrinity_InterruptGetEnabled(XTrinity *InstancePtr) {
    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    return XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_IER);
}

u32 XTrinity_InterruptGetStatus(XTrinity *InstancePtr) {
    Xil_AssertNonvoid(InstancePtr != NULL);
    Xil_AssertNonvoid(InstancePtr->IsReady == XIL_COMPONENT_IS_READY);

    return XTrinity_ReadReg(InstancePtr->Control_BaseAddress, XTRINITY_CONTROL_ADDR_ISR);
}

