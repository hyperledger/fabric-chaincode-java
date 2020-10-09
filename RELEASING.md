# Releasing

The following artifacts are created as a result of releasing Fabric Chaincode Java:

- docker images
    - [fabric-javaenv](https://hub.docker.com/r/hyperledger/fabric-javaenv)
- Java libraries
    - [fabric-chaincode-shim](https://search.maven.org/search?q=a:fabric-chaincode-shim)
    - [fabric-chaincode-protos](https://search.maven.org/search?q=a:fabric-chaincode-protos)

**Note:** A docker image with a matching V.R version is required before releasing a new version of Fabric.

## Before releasing

It's useful to create an issue to keep track of each release, for example [Release 1.4.6 Java Chaincode](https://jira.hyperledger.org/browse/FABCJ-281).

The following tasks are required before releasing:

- Update version numbers in `build.gradle` files to the required version
- Update test, sample, and docs files to match the new version
- Create a new release notes file
- Update the `CHANGELOG.md` file
  
  The `changelog.sh` script in `scripts` will prepopulate the changelog but you must check and edit the file manually afterwards as required

See the [Prepare 1.4.7 release](https://github.com/hyperledger/fabric-chaincode-java/pull/139) pull request for an example, although be careful to search for all versions in the codebase as they're easy to miss and things change!

## Create release

Creating a GitHub release on the [releases page](https://github.com/hyperledger/fabric-chaincode-java/releases) will trigger the build to publish the new release.

When drafting the release, create a new tag for the new version (with a `v` prefix), e.g. `v1.4.6`

See previous releases for examples of the title and description.

## Publish Java libraries

Log on to the [nexus repository manager](https://oss.sonatype.org/#welcome) to manually publish the JARs which were pushed by the release build.

Find the results of the release build under _Build Promotion > Staging Repositories_ and perform the following steps:

1. Close

   You should see a series of close activities (see note)

2. Release using the automatically drop option

   You should see a series of release activities (see note)

Note: you may need to refresh to update the activities view.

When the release has completed and the _Staging Repositories_ list is empty, the Java chaincode libraries should appear in the maven repository. They can take some time to appear in the UI but they should exist in the repository sooner.

## After releasing

- Update version numbers in `build.gradle` files to the next version
- Update test, sample, and docs files to match the new version

See the [[FABCJ-281] Set to the next version 1.4.7](https://github.com/hyperledger/fabric-chaincode-java/pull/90) pull request for an example. It should include almost all the files changed to prepare for the release, except for the release notes and changelog which do not need updating.
