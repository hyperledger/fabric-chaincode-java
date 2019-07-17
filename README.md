# Hyperledger Fabric - Java Contract and Chaincode

This is a Java implementation of Hyperledger Fabric chaincode shim APIs and contract programming model.  This enables development of using Java language or other JVM based languages

## Developers

Application developers interested in developing smart contracts should read the [introductory tutorial](CONTRACT_TUTORIAL.md) and for a full scenario visit the
[Commercial Paper](https://hyperledger-fabric.readthedocs.io/en/latest/tutorial/commercial_paper.html) tutorial.

We recommend using the IBM Blockchain Platform [VSCode extension](https://marketplace.visualstudio.com/items?itemName=IBMBlockchain.ibm-blockchain-platform) to assist you in developing smart contracts. The extension is able to create sample contracts for you, an example of such a contract is in the [fabric-contract-example](./fabric-contract-example) directory; there are folders for using both gradle and maven.

In addition, this has reference to other tutorials to get you started

## Contributors

Contributors or early adopters who need to be able to build and test recent builds, should reference the [contributing](CONTRIBUTING.md) guide.

This project creates `fabric-chaincode-protos` and `fabric-chaincode-shim` jar files for developers consumption and the `hyperledger/fabric-javaenv` docker image to run the Java chaincode and contracts.

