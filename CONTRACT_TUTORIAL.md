## Contract tutorials

This tutorial will teach you how to write Java based Hyperledger Fabric Contract; the code examples have been created using the IBM Blockchain Platform VSCode extension.

## Writing your own contract

Either gradle or maven can be used for building your code; here the example is shown with gradle.

### Create Gradle project
You can use `fabric-contract-example/gradle` as staring point. Make sure that your project build creates a runnable jar that contains all dependencies named `chaincode.jar` as result.

The main class is very important for Contracts and must be set to `org.hyperledger.fabric.contract.ContractRouter`.  All these are set correctly in the examples but for reference the important parts are

```
plugins {
    id 'com.github.johnrengelman.shadow' version '2.0.3'
}
...

...
shadowJar {
    baseName = 'chaincode'
    version = null
    classifier = null

    manifest {
        attributes 'Main-Class': 'org.hyperledger.fabric.contract.ContractRouter'
    }
}
```

### Writing the contract

Typically a Contract will be working with one or more 'assets', so in this example we are using a `MyAssestContract` class.  Within a given chaincode container,
 (the docker container that starts when chaincode is instantiated), you can have one or more contract classes. Each contract class has one or more
 'transaction functions' these can be executed from the SDK or from other contracts.

With the VSCode extension you can create a starter project, or you can use the same Yeoman generator from the command line.

```
# if you don't have Yeoman already
npm install -g yo

npm install -g generator-fabric
```

You can then run the generator to create a sample Java Contract project that uses Gradle 4.6
This is an example output of running the generator

```
 yo fabric:contract
? Please specify the contract language: Java
? Please specify the contract name: MyJavaContract
? Please specify the contract version: 0.0.1
? Please specify the contract description: My first Java contract
? Please specify the contract author: ANOther
? Please specify the contract license: Apache-2.0
? Please specify the asset type: MyAsset
```

### Code overview

As well as the gradle project files, a `org.example.MyAsset.java` and a `org.example.MyAssetContract.java` are
created.

All contract classes like `MyAssetContract` must implement the `ContractInterface` and have a `@Contract` annotation
to mark this as a Contract.  The annotation allows you to specify meta information, such as version, author
and description. Refer to the JavaDoc for the full specificaiton.

The `@Default` annotation is useful, as it marks the class as a the default contract - when referring to the
transaction function from the client SDK it permits a shorthand to be used.

It is recommened to have a no-argument constructor in the contract.

Each method you wish to be a transaction function must be marked by a `@Transaction()` annotation.
The first parameter to each of these must accept a `org.hyperledger.fabric.contract.Context` object. This
object is the 'transactional context' and provides information about the current transaction being executed
and also provides access to the APIs to check and update the ledger state.

Transaciton functions can have as many other parameters as they wish.

Standard Java primitives and strings can be passed; any other Java Object IS NOT supported, eg passing a HashMap.
More complex types can be defined if they are suitably defined.   Arrays are supported types are permitted.

The `MyAsset` class is an example of the more a complex datatype that can be passed. Such a class is
marked used the `@DataType` annotation, with each property within the object marked by a `@Property` annotation.

Richer constraints on datatype and parameters can be applied; see the JavaDoc for details.

#### Building contract

Run build command.

```bash
gradle clean build shadowJar
```
Assuming there are no build errors, you can proceed to chaincode testing.

