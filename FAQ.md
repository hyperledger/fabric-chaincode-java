# Frequently Asked Questions - Hyperledger Fabric Shim for Java chaincode

### Q. How to build latest master code?

#### Install prerequisites

Make sure you installed all [fabric prerequisites](https://hyperledger-fabric.readthedocs.io/en/latest/prereqs.html)

Install java shim specific prerequisites:
* Java 8
* gradle 4.4

#### Build shim

Clone the fabric shim for java chaincode repo.

```
git clone https://github.com/hyperledger/fabric-chaincode-java.git
```

Build java shim jars (proto and shim jars) and install them to local maven repository.
```
cd fabric-chaincode-java
gradle clean build install
```

Build javaenv docker image, to have it locally.
```
gradle buildImage
```

#### Update your chaincode dependencies

Make your chanincode depend on java shim master version and not on version from maven central

```
dependencies {
    compile group: 'org.hyperledger.fabric-chaincode-java', name: 'fabric-chaincode-shim', version: '1.4.2-SNAPSHOT'
}
```