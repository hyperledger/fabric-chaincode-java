## Contributing

We welcome contributions to the Hyperledger Fabric Project in many forms, and
there's always plenty to do!

Please visit the
[contributors guide](http://hyperledger-fabric.readthedocs.io/en/latest/CONTRIBUTING.html) in the
docs to learn how to make contributions to this exciting project.

## Folder structure

The "fabric-chaincode-protos" folder contains the protobuf definition files used by
Java shim to communicate with Fabric peer.

The "fabric-chaincode-shim" folder contains the java shim classes that define Java
chaincode API and way to communicate with Fabric peer.

The "fabric-chaincode-docker" folder contains instructions to the build
`hyperledger/fabric-javaenv` docker image.

The "fabric-chaincode-example-gradle" contains an example java chaincode gradle
project that includes sample chaincode and basic gradle build instructions.

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

## Code of Conduct Guidelines <a name="conduct"></a>

See our [Code of Conduct Guidelines](../blob/master/CODE_OF_CONDUCT.md).

## Maintainers <a name="maintainers"></a>

Should you have any questions or concerns, please reach out to one of the project's [Maintainers](../blob/master/MAINTAINERS.md).

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
