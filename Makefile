#
# SPDX-License-Identifier: Apache-2.0
#

.PHONEY: scan
scan:
	go install github.com/google/osv-scanner/cmd/osv-scanner@latest
	./gradlew cyclonedxBom
	osv-scanner --sbom='fabric-chaincode-shim/build/reports/bom.json'
