# Hyperledger Fabric Chaincode Java

[![Build Status](https://dev.azure.com/Hyperledger/Fabric-Chaincode-Java/_apis/build/status/Fabric-Chaincode-Java?branchName=master)](https://dev.azure.com/Hyperledger/Fabric-Chaincode-Java/_build/latest?definitionId=39&branchName=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.hyperledger.fabric-chaincode-java/fabric-chaincode-shim/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.hyperledger.fabric-chaincode-java/fabric-chaincode-shim)
[![Rocket.Chat](https://chat.hyperledger.org/images/join-chat.svg)](https://chat.hyperledger.org/channel/fabric-java-chaincode)

This is a Java based implementation of Hyperledger Fabric chaincode shim APIs, which enables development of smart contracts using the Java language.

This project creates `fabric-chaincode-protos` and `fabric-chaincode-shim` jar
files for developers' consumption and the `hyperledger/fabric-javaenv` docker image
to run Java chaincode.

## Getting Started

Application developers interested in developing Java smart contracts for Hyperledger Fabric should read the [JavaDoc](https://hyperledger.github.io/fabric-chaincode-java/) which includes download information, and links to documentation and samples.

## Project structure

### fabric-chaincode-protos

Contains the protobuf definition files used by Java shim to communicate with Fabric peers.

### fabric-chaincode-shim

Contains the java shim classes that define Java chaincode API and way to communicate with Fabric peers.

### fabric-chaincode-docker

Contains instructions to build the `hyperledger/fabric-javaenv` docker image.

### fabric-chaincode-integration-test

Contains higher level tests for Java chaincode.

> **Note:** in the future these should be replaced with a separate suite of [Cucumber](https://cucumber.io) tests which run against all chaincode implementations.

### examples

The following technical examples are in this repository. Please see the tutorials in the [documentation](https://hyperledger-fabric.readthedocs.io/en/latest/tutorial/commercial_paper.html)


- **fabric-contract-example-gradle**  -  Contains an example Java contract built using gradle
- **fabric-contract-example-maven**  -  Contains an example Java contract built using maven
- **fabric-contract-example-gradle-kotlin**  -  Contains an example Kotlin contract build using gradle (Kotlin gradle files)
- **fabric-chaincode-example-sacc**  -  Contains an example java chaincode gradle project that includes sample chaincode and basic gradle build instructions.
- **fabric-chaincode-example-sbe**  -  Contains an example java chaincode gradle project that includes state based endorsement

  


## Building and testing

Make sure you have the following prereqs installed:

- [Docker](https://www.docker.com/get-docker)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Java 8](https://www.java.com)

> **Note:** Java can be installed using [sdkman](https://sdkman.io/).

Clone the repository if you haven't already.

```
git clone https://github.com/hyperledger/fabric-chaincode-java.git
```

Build java shim jars (proto and shim jars) and install them to local maven repository.

```
cd fabric-chaincode-java
./gradlew clean build install
```

> **Note:** `./gradlew clean build classes` can be used instead to reduce the binaries that are built. This should be sufficient for using the local repository.

Build javaenv docker image, to have it locally.

```
./gradlew buildImage
```

## Compatibility

For details on what Java runtime and versions of Hyperledger Fabric can be used please see the [compatibility document](COMPATIBILITY.md).

---

[![Creative Commons License](https://i.creativecommons.org/l/by/4.0/88x31.png)](http://creativecommons.org/licenses/by/4.0/)  
This work is licensed under a [Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/)
