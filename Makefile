#
# SPDX-License-Identifier: Apache-2.0
#

.PHONEY: scan
scan:
	go install github.com/google/osv-scanner/cmd/osv-scanner@latest
	./gradlew --quiet resolveAndLockAll --write-locks
	osv-scanner scan --lockfile=fabric-chaincode-shim/gradle.lockfile
