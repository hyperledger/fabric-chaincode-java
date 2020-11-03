---
layout: home
---

Hyperledger Fabric offers a number of SDKs to support developing smart contracts (chaincode)
in various programming languages. There are two other smart contract SDKs available for Go, and Node.js, in addition to this Java SDK:

  * [Go SDK documentation](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim)
  * [Node.js SDK documentation](https://hyperledger.github.io/fabric-chaincode-node/)

## Documentation

Detailed explanation on the concepts and programming model for smart contracts can be found in the [Chaincode Tutorials section of the Hyperledger Fabric documentation](https://hyperledger-fabric.readthedocs.io/en/latest/developapps/smartcontract.html#).

Javadoc is available for each release:

{% include javadocs.html %}

## Download

Gradle:

```
dependencies {
  implementation 'org.hyperledger.fabric-chaincode-java:fabric-chaincode-shim:VERSION'
}
```

Maven:

```
<dependency>
  <groupId>org.hyperledger.fabric-chaincode-java</groupId>
  <artifactId>fabric-chaincode-shim</artifactId>
  <version>VERSION</version>
</dependency>
```

More options can be found on the [central maven repository](https://search.maven.org/artifact/org.hyperledger.fabric-chaincode-java/fabric-chaincode-shim/).

Check the [release notes](https://github.com/hyperledger/fabric-chaincode-java/releases) for the changes in each version.

## Compatibility

For details on what versions of Java and Hyperledger Fabric can be used please see the [compatibility document](COMPATIBILITY.md).

## Samples

Java chaincode samples for commercial paper and fabcar can be found in the [fabric-samples repository](https://github.com/hyperledger/fabric-samples)
