# Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
# SPDX-License-Identifier: MIT

TOP = TrinitySdl

HDL_SRCS = ../generated/$(TOP).sv
CPP_SRCS = main.cpp

PRODUCT = trinity
VFLAGS  = -O3
LDFLAGS = -lSDL2

all: obj_dir/$(PRODUCT)

run: obj_dir/$(PRODUCT)
	obj_dir/$(PRODUCT)

obj_dir: $(HDL_SRCS) | $(CPP_SRCS)
	@printf " RM\tobj_dir\n"
	@rm -rf obj_dir
	@printf " VERILATOR\t$(HDL_SRCS) $(CPP_SRCS)\n"
	@verilator --exe $(HDL_SRCS) --cc $(CPP_SRCS) -o $(PRODUCT) \
			   $(VFLAGS) -LDFLAGS $(LDFLAGS)

obj_dir/$(PRODUCT): obj_dir
	@printf " MAKE\tobj_dir\n"
	@make -C obj_dir -f V$(TOP).mk

clean:
	@printf " RM\tobj_dir\n"
	@rm -rf obj_dir
