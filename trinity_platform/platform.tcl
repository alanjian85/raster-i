# 
# Usage: To re-create this platform project launch xsct with below options.
# xsct /home/alanjian85/trinity/trinity_platform/platform.tcl
# 
# OR launch xsct and run below command.
# source /home/alanjian85/trinity/trinity_platform/platform.tcl
# 
# To create the platform in a different location, modify the -out option of "platform create" command.
# -out option specifies the output directory of the platform project.

platform create -name {trinity_platform}\
-hw {/home/alanjian85/trinity/rtl/trinity_rtl.xsa}\
-arch {64-bit} -fsbl-target {psu_cortexa53_0} -out {/home/alanjian85/trinity}

platform write
domain create -name {standalone_psu_cortexa53_0} -display-name {standalone_psu_cortexa53_0} -os {standalone} -proc {psu_cortexa53_0} -runtime {cpp} -arch {64-bit} -support-app {hello_world}
platform generate -domains 
platform active {trinity_platform}
domain active {zynqmp_fsbl}
domain active {zynqmp_pmufw}
domain active {standalone_psu_cortexa53_0}
platform generate -quick
bsp reload
platform generate
platform active {trinity_platform}
platform config -updatehw {/home/alanjian85/trinity/rtl/trinity_rtl.xsa}
bsp reload
platform generate
platform active {trinity_platform}
platform config -updatehw {/home/alanjian85/trinity/rtl/trinity_rtl.xsa}
platform generate -domains 
