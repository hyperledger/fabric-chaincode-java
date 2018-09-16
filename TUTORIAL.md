## Chaincode tutorials
This tutorial will teach you how to write Java based Hyperledger Fabric chaincode.
For general explanation about chaincode, how to write and operate it, please visit [Chaincode Tutorials](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode.html)

## Writing your own chaincode
Writing your own chaincode requires understanding Fabric platform, Java and Gradle.

### Create Gradle project
You can use `fabric-chaincode-example-gradle` as staring point. Make sure that
your project build creates a runnable jar that contains all dependencies named `chaincode.jar` as result.

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
        attributes 'Main-Class': 'your.chaincode.class.name'
    }
}
```

### Writing chaincode
We will use the Java version of [Simple Asset Chaincode](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html#simple-asset-chaincode) as an example.
This chaincode is a Go to Java translation of Simple Asset Chaincode, which we will explain.

Using a chaincode class of `org.hyperledger.fabric.example.SimpleAsset`,
create the Java file
`src/main/java/org/hyperledger/fabric/example/SimpleAsset.java` inside your
Gradle project.

#### Housekeeping
Your chaincode should implement the `Chaincode` interface (or extend
`ChaincodeBase` abstract class) in order to use the  `ChaincodeStub` API to
access proposal and ledger data.

`ChaincodeBase` class is abstract class which inherits form  `Chaincode` which
contains the `start` method used to start chaincode. Therefore, we will create
our chaincode by extending `ChaincodeBase` instead of implementing `Chaincode`.

```java
package org.hyperledger.fabric.example;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
public class SimpleAsset extends ChaincodeBase {
        @Override
        public Response init(ChaincodeStub stub) {
            return newSuccessResponse();
        }

        @Override
        public Response invoke(ChaincodeStub stub) {
            return newSuccessResponse();
        }

}

```

#### Initializing the Chaincode

Chaincode initialization is done inside the
` Response init(ChaincodeStub stub)` method. First, retrieve arguments using
`ChaincodeStub.getStringArgs()` method.
```java
    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        // Get the args from the transaction proposal
        List<String> args = stub.getStringArgs();
        if (args.size() != 2) {
            newErrorResponse("Incorrect arguments. Expecting a key and a value");
        }
        return newSuccessResponse();
    }
```

After that, store state to ledger using using
`ChaincodeStub.putStringState(key, value)` method.
```java
    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        try {
            // Get the args from the transaction proposal
            List<String> args = stub.getStringArgs();
            if (args.size() != 2) {
                newErrorResponse("Incorrect arguments. Expecting a key and a value");
            }
            // Set up any variables or assets here by calling stub.putState()
            // We store the key and the value on the ledger
            stub.putStringState(args.get(0), args.get(1));
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse("Failed to create asset");
        }
    }
```

#### Invoking the Chaincode

Chaincode invokation is done inside the `Response invoke(ChaincodeStub stub)`
method.
```java
    /**
     * Invoke is called per transaction on the chaincode. Each transaction is
     * either a 'get' or a 'set' on the asset created by Init function. The Set
     * method may create a new asset by specifying a new key-value pair.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        return newSuccessResponse();
    }
```

Extract the function name and arguments using `ChaincodeStub.getFunction()` and
`ChaincodeStub.getParameters()` methods. Validate function name and invoke
corresponding chaincode methods. The value received by the chaincode methods should be returned as
a success response payload. In case of an exception or incorrect function value,
return an error response.

```java
    public Response invoke(ChaincodeStub stub) {
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("set")) {
                // Return result as success payload
                return newSuccessResponse(set(stub, params));
            } else if (func.equals("get")) {
                // Return result as success payload
                return newSuccessResponse(get(stub, params));
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"set\", \"get\"");
        } catch (Throwable e) {
            return newErrorResponse(e.getMessage());
        }
    }

```

#### Implementing the Chaincode methods

Implement methods `set()` and `get()` using
`ChaincodeStub.putStringState(key, value)` and
`ChaincodeStub.getStringState(key)`.

```java
    /**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    private String get(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        String value = stub.getStringState(args.get(0));
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    private String set(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        stub.putStringState(args.get(0), args.get(1));
        return args.get(1);
    }

```

#### Putting it All Together

Finally, add `main()` method to start chaincode.

```java
package org.hyperledger.fabric.example;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
public class SimpleAsset extends ChaincodeBase {

    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        try {
            // Get the args from the transaction proposal
            List<String> args = stub.getStringArgs();
            if (args.size() != 2) {
                newErrorResponse("Incorrect arguments. Expecting a key and a value");
            }
            // Set up any variables or assets here by calling stub.putState()
            // We store the key and the value on the ledger
            stub.putStringState(args.get(0), args.get(1));
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse("Failed to create asset");
        }
    }

    /**
     * Invoke is called per transaction on the chaincode. Each transaction is
     * either a 'get' or a 'set' on the asset created by Init function. The Set
     * method may create a new asset by specifying a new key-value pair.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("set")) {
                // Return result as success payload
                return newSuccessResponse(set(stub, params));
            } else if (func.equals("get")) {
                // Return result as success payload
                return newSuccessResponse(get(stub, params));
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"set\", \"get\"");
        } catch (Throwable e) {
            return newErrorResponse(e.getMessage());
        }
    }

    /**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    private String get(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        String value = stub.getStringState(args.get(0));
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    private String set(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        stub.putStringState(args.get(0), args.get(1));
        return args.get(1);
    }

    public static void main(String[] args) {
        new SimpleAsset().start(args);
    }

}
```

#### Building chaincode

Run build command.

```bash
gradle clean build shadowJar
```
Assuming there are no build errors, you can proceed to chaincode testing.

#### Testing chaincode

First, install the chaincode. The peer CLI will package the Java chaincode source
(src folder) and Gradle build scripts and send them to the peer to install. If
you have previously installed a chaincode called by the same name and version,
you can delete it from the peer by removing the file
`/var/hyperledger/production/chaincodes/<name>.<version>`.
```
CORE_LOGGING_PEER=debug ./build/bin/peer chaincode install -l java -n mycc -v v0 -p <path to chaincode folder>
```

Upon successful response, instantiate the chaincode on the "test" channel
created above:
```
CORE_LOGGING_PEER=debug ./build/bin/peer chaincode instantiate -o localhost:7050 -C mychannel -l java -n mycc -v v0 -c '{"Args":["init"]}' -P 'OR ("Org1MSP.member")'
```

This will take a while to complete as the peer must perform "docker pull" to download
java specific image in order to build and launch the chaincode. When successfully
completed, you should see in the peer's log a message confirming the committing
of a new block. This new block contains the transaction to instantiate the
chaincode `mycc:v0`.

To further inspect the result of the chaincode instantiate command, run
`docker images` and you will see a new image listed at the top of the list with
the name starting with `dev-`. You can inspect the content of this image by
running the following command:
```
docker run -it dev-jdoe-mycc-v0 bash
root@c188ae089ee5:/# ls /root/chaincode-java/chaincode
chaincode.jar
root@c188ae089ee5:/#
```
