# Hyperledger Fabric Shim for Java chaincode

This is a Java based implementation of Hyprledger Fabric chaincode shim APIs, which enables development of chaincodes using Java language.

Application developers interested in developing smart contracts (what we call chaincode) for Hyperledger Fabric should
read the tutorial in TUTORIAL.md file and visit
[Chaincode for developers](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html).

Contributors or early adopters who need to be able to build and test recent Java chaincode shim, should reference [FAQ.md](FAQ.md) file.

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

# Compatibility
Java SDK 8 or above