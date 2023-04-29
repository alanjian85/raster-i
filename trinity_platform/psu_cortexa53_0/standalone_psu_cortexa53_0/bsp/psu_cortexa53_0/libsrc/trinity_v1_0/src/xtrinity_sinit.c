// ==============================================================
// Vitis HLS - High-Level Synthesis from C, C++ and OpenCL v2022.2.2 (64-bit)
// Tool Version Limit: 2019.12
// Copyright 1986-2023 Xilinx, Inc. All Rights Reserved.
// ==============================================================
#ifndef __linux__

#include "xstatus.h"
#include "xparameters.h"
#include "xtrinity.h"

extern XTrinity_Config XTrinity_ConfigTable[];

XTrinity_Config *XTrinity_LookupConfig(u16 DeviceId) {
	XTrinity_Config *ConfigPtr = NULL;

	int Index;

	for (Index = 0; Index < XPAR_XTRINITY_NUM_INSTANCES; Index++) {
		if (XTrinity_ConfigTable[Index].DeviceId == DeviceId) {
			ConfigPtr = &XTrinity_ConfigTable[Index];
			break;
		}
	}

	return ConfigPtr;
}

int XTrinity_Initialize(XTrinity *InstancePtr, u16 DeviceId) {
	XTrinity_Config *ConfigPtr;

	Xil_AssertNonvoid(InstancePtr != NULL);

	ConfigPtr = XTrinity_LookupConfig(DeviceId);
	if (ConfigPtr == NULL) {
		InstancePtr->IsReady = 0;
		return (XST_DEVICE_NOT_FOUND);
	}

	return XTrinity_CfgInitialize(InstancePtr, ConfigPtr);
}

#endif

