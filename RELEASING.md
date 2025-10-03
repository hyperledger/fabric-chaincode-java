# Releasing

The following artifacts are created as a result of releasing Fabric Chaincode Java:

- `fabric-javaenv` Docker images:
  - [Docker Hub](https://hub.docker.com/r/hyperledger/fabric-javaenv)
  - [GitHub Packages](https://github.com/orgs/hyperledger/packages/container/package/fabric-javaenv)
- `fabric-chaincode-shim` Java libraries:
  - [Maven Central](https://central.sonatype.com/artifact/org.hyperledger.fabric-chaincode-java/fabric-chaincode-shim)
  - [GitHub Packages](https://github.com/hyperledger/fabric-chaincode-java/packages/50049)

## Before releasing

The following tasks are required before releasing:

- Ensure the version number in `build.gradle` is the required release version.
- Check the last branch build passed since exactly this repository state will be released.

## Create release

Creating a GitHub release on the [releases page](https://github.com/hyperledger/fabric-chaincode-java/releases) will trigger the build to publish the new release.

When drafting the release, create a new tag for the new version (with a `v` prefix). For example: `v2.1.4`

See previous releases for examples of the title and description.

## After releasing

- Update the version number in `build.gradle` to the next version.
- Update image version numbers in `fabric-chaincode-docker/build.gradle` to match the next version.
- Update the `fabric-chaincode-shim` dependency version in all `build.gradle` and `pom.xml` files within `fabric-chaincode-integration-test/src/contracts` to match the next version.
- Update the `fabric-chaincode-shim` dependency version in all `build.gradle`, `build.gradle.kts` and `pom.xml` files within `examples` to match the last _released_ version.
- Check that `COMPATIBILITY.md` is correct and update if required.
