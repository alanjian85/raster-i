BUILDDIR := ${CURDIR}/build

SOURCES := ${CURDIR}/top.v

XDC := ${CURDIR}/arty.xdc

all: ${BUILDDIR}/top.bit

${BUILDDIR}:
	mkdir -p ${BUILDDIR}

${BUILDDIR}/top.eblif: ${SOURCES} ${XDC} | ${BUILDDIR}
	cd ${BUILDDIR} && symbiflow_synth -t top  -v ${SOURCES} -d artix7 -p xc7a100tcsg324-1 -x ${XDC}

${BUILDDIR}/top.net: ${BUILDDIR}/top.eblif
	cd ${BUILDDIR} && symbiflow_pack -e top.eblif -d xc7a100t_test

${BUILDDIR}/top.place: ${BUILDDIR}/top.net
	cd ${BUILDDIR} && symbiflow_place -e top.eblif -d xc7a100t_test -n top.net -P xc7a100tcsg324-1

${BUILDDIR}/top.route: ${BUILDDIR}/top.place
	cd ${BUILDDIR} && symbiflow_route -e top.eblif -d xc7a100t_test

${BUILDDIR}/top.fasm: ${BUILDDIR}/top.route
	cd ${BUILDDIR} && symbiflow_write_fasm -e top.eblif -d xc7a100t_test

${BUILDDIR}/top.bit: ${BUILDDIR}/top.fasm
	cd ${BUILDDIR} && symbiflow_write_bitstream -d artix7 -f top.fasm -p xc7a100tcsg324-1 -b top.bit

download: ${BUILDDIR}/top.bit
	openFPGALoader -b arty_a7_100t ${BUILDDIR}/top.bit

clean:
	rm -rf ${BUILDDIR}
