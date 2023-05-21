# Usage with Vitis IDE:
# In Vitis IDE create a Single Application Debug launch configuration,
# change the debug type to 'Attach to running target' and provide this 
# tcl script in 'Execute Script' option.
# Path of this script: /home/alanjian85/trinity/trinity_app_system/_ide/scripts/systemdebugger_trinity_app_system_standalone.tcl
# 
# 
# Usage with xsct:
# To debug using xsct, launch xsct and run below command
# source /home/alanjian85/trinity/trinity_app_system/_ide/scripts/systemdebugger_trinity_app_system_standalone.tcl
# 
connect -url tcp:127.0.0.1:3121
source /tools/Xilinx/Vitis/2022.2/scripts/vitis/util/zynqmp_utils.tcl
targets -set -nocase -filter {name =~"APU*"}
rst -system
after 3000
targets -set -filter {jtag_cable_name =~ "Xilinx X-MLCC-01 XFL13UA1UYB2A" && level==0 && jtag_device_ctx=="jsn-X-MLCC-01-XFL13UA1UYB2A-04724093-0"}
fpga -file /home/alanjian85/trinity/trinity_app/_ide/bitstream/trinity_rtl.bit
targets -set -nocase -filter {name =~"APU*"}
loadhw -hw /home/alanjian85/trinity/trinity_platform/export/trinity_platform/hw/trinity_rtl.xsa -mem-ranges [list {0x80000000 0xbfffffff} {0x400000000 0x5ffffffff} {0x1000000000 0x7fffffffff}] -regs
configparams force-mem-access 1
targets -set -nocase -filter {name =~"APU*"}
set mode [expr [mrd -value 0xFF5E0200] & 0xf]
mask_write 0xFF5E0200 0xf000 0
targets -set -nocase -filter {name =~ "*A53*#0"}
rst -processor
dow /home/alanjian85/trinity/trinity_platform/export/trinity_platform/sw/trinity_platform/boot/fsbl.elf
set bp_42_45_fsbl_bp [bpadd -addr &XFsbl_Exit]
con -block -timeout 60
bpremove $bp_42_45_fsbl_bp
targets -set -nocase -filter {name =~ "*A53*#0"}
rst -processor
dow /home/alanjian85/trinity/trinity_app/Debug/trinity_app.elf
configparams force-mem-access 0
targets -set -nocase -filter {name =~ "*A53*#0"}
con
