# Hyperledger Fabric Shim for Java chaincode

This is a Java based implementation of Hyprledger Fabric chaincode shim APIs, which enables development of chaincodes using Java language.
The following instructions are intended for a contributor or early adopter that needs to
be able to build and test the Java chaincode shim, docker image and protobuf artifacts.

Application developers interested in developing smart contracts (what we call chaincode) for Hyperledger Fabric should
read the tutorial in TUTORIAL.md file and visit
`Chaincode for developers <https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html>`__.

This project creates `fabric-chaincode-protos` and `fabric-chaincode-shim` jar
files for developers consumption and the `hyperledger/fabric-javaenv` docker image
to run Java chaincode.

## Folder structure

The "fabric-chaincode-protos" folder contains the protobuf definition files used by
Java shim to communicate with Fabric peer.

The "fabric-chaincode-shim" folder contains the java shim classes that define Java
chaincode API and way to communicate with Fabric peer.

The "fabric-chaincode-docker" folder contains instructions to the build
`hyperledger/fabric-javaenv` docker image.

The "fabric-chaincode-example-gradle" contains an example java chaincode gradle
project that includes sample chaincode and basic gradle build instructions.

## Prerequisites
* Java 8
* gradle 4.4

## Build shim

Clone the fabric shim for java chaincode repo.

```
git clone https://github.com/hyperledger/fabric-chaincode-java.git
```

Build and install java shim jars (proto and shim jars).
```
cd fabric-chaincode-java
gradle clean build install
```

Build javaenv docker image, to have it locally.
```
gradle buildImage
```
