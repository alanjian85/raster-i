// ==============================================================
// Vitis HLS - High-Level Synthesis from C, C++ and OpenCL v2022.2.2 (64-bit)
// Tool Version Limit: 2019.12
// Copyright 1986-2023 Xilinx, Inc. All Rights Reserved.
// ==============================================================
#ifndef XTRINITY_H
#define XTRINITY_H

#ifdef __cplusplus
extern "C" {
#endif

/***************************** Include Files *********************************/
#ifndef __linux__
#include "xil_types.h"
#include "xil_assert.h"
#include "xstatus.h"
#include "xil_io.h"
#else
#include <stdint.h>
#include <assert.h>
#include <dirent.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stddef.h>
#endif
#include "xtrinity_hw.h"

/**************************** Type Definitions ******************************/
#ifdef __linux__
typedef uint8_t u8;
typedef uint16_t u16;
typedef uint32_t u32;
typedef uint64_t u64;
#else
typedef struct {
    u16 DeviceId;
    u64 Control_BaseAddress;
} XTrinity_Config;
#endif

typedef struct {
    u64 Control_BaseAddress;
    u32 IsReady;
} XTrinity;

typedef u32 word_type;

/***************** Macros (Inline Functions) Definitions *********************/
#ifndef __linux__
#define XTrinity_WriteReg(BaseAddress, RegOffset, Data) \
    Xil_Out32((BaseAddress) + (RegOffset), (u32)(Data))
#define XTrinity_ReadReg(BaseAddress, RegOffset) \
    Xil_In32((BaseAddress) + (RegOffset))
#else
#define XTrinity_WriteReg(BaseAddress, RegOffset, Data) \
    *(volatile u32*)((BaseAddress) + (RegOffset)) = (u32)(Data)
#define XTrinity_ReadReg(BaseAddress, RegOffset) \
    *(volatile u32*)((BaseAddress) + (RegOffset))

#define Xil_AssertVoid(expr)    assert(expr)
#define Xil_AssertNonvoid(expr) assert(expr)

#define XST_SUCCESS             0
#define XST_DEVICE_NOT_FOUND    2
#define XST_OPEN_DEVICE_FAILED  3
#define XIL_COMPONENT_IS_READY  1
#endif

/************************** Function Prototypes *****************************/
#ifndef __linux__
int XTrinity_Initialize(XTrinity *InstancePtr, u16 DeviceId);
XTrinity_Config* XTrinity_LookupConfig(u16 DeviceId);
int XTrinity_CfgInitialize(XTrinity *InstancePtr, XTrinity_Config *ConfigPtr);
#else
int XTrinity_Initialize(XTrinity *InstancePtr, const char* InstanceName);
int XTrinity_Release(XTrinity *InstancePtr);
#endif

void XTrinity_Start(XTrinity *InstancePtr);
u32 XTrinity_IsDone(XTrinity *InstancePtr);
u32 XTrinity_IsIdle(XTrinity *InstancePtr);
u32 XTrinity_IsReady(XTrinity *InstancePtr);
void XTrinity_EnableAutoRestart(XTrinity *InstancePtr);
void XTrinity_DisableAutoRestart(XTrinity *InstancePtr);

void XTrinity_Set_sine(XTrinity *InstancePtr, u32 Data);
u32 XTrinity_Get_sine(XTrinity *InstancePtr);

void XTrinity_InterruptGlobalEnable(XTrinity *InstancePtr);
void XTrinity_InterruptGlobalDisable(XTrinity *InstancePtr);
void XTrinity_InterruptEnable(XTrinity *InstancePtr, u32 Mask);
void XTrinity_InterruptDisable(XTrinity *InstancePtr, u32 Mask);
void XTrinity_InterruptClear(XTrinity *InstancePtr, u32 Mask);
u32 XTrinity_InterruptGetEnabled(XTrinity *InstancePtr);
u32 XTrinity_InterruptGetStatus(XTrinity *InstancePtr);

#ifdef __cplusplus
}
#endif

#endif
