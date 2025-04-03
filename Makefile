#
# SPDX-License-Identifier: Apache-2.0
#

bin_dir := bin
osv-scanner := $(bin_dir)/osv-scanner

kernel_name := $(shell uname -s | tr '[:upper:]' '[:lower:]')
machine_hardware := $(shell uname -m)
ifeq ($(machine_hardware), x86_64)
	machine_hardware := amd64
endif
ifeq ($(machine_hardware), aarch64)
	machine_hardware := arm64
endif

.PHONY: scan
scan: $(osv-scanner)
	./gradlew --quiet :fabric-chaincode-shim:dependencies --write-locks --configuration runtimeClasspath
	bin/osv-scanner scan --lockfile=fabric-chaincode-shim/gradle.lockfile

.PHONY: install-osv-scanner
install-osv-scanner:
	mkdir -p '$(bin_dir)'
	curl --fail --location --show-error --silent --output '$(osv-scanner)' \
    		'https://github.com/google/osv-scanner/releases/latest/download/osv-scanner_$(kernel_name)_$(machine_hardware)'
	chmod u+x '$(osv-scanner)'

$(osv-scanner):
	$(MAKE) install-osv-scanner
