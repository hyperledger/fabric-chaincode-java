# Support and Compatibility

Github is used for code base management and issue tracking.

## Summary of Compatibility

This table shows the summary of the compatibility of the Java chaincode libraries, together with the JVM version they require.

| Java chaincode version | Minimum supported Java | Java runtime | Docker image platforms |
|------------------------|------------------------|--------------|------------------------|
| v1.4                   | 8                      | 8            | amd64                  |
| v2.2                   | 11                     | 11           | amd64                  |
| v2.5.0 - v2.5.4        | 11                     | 11           | amd64, arm64           |
| v2.5.5+                | 11                     | 21           | amd64, arm64           |

The Java runtime provided by the chaincode Docker image determines the maximum Java version (and features) that smart contract code can exploit when using the default Java chaincode container.

Subject to a suitable runtime environment, the Java chaincode libraries can be used to communicate with Fabric peers at different LTS versions. The level of functionality is determined by the Fabric version in use and channel capabilities.

All Docker images, chaincode libraries and tools are tested using amd64 (x86-64) only.

## Chaincode builder

The default Fabric chaincode builder creates a Docker container to run deployed smart contracts. Java chaincode Docker containers are built using the `hyperledger/fabric-javaenv` Docker image, tagged with the same major and minor version as the Fabric peer version. For example, Fabric v2.5 creates Java chaincode containers using the `hyperledger/fabric-javaenv:2.5` Docker image. Fabric v3 continues to use the v2.5 Java chaincode image.

A different chaincode Docker image can be specified using the `CORE_CHAINCODE_JAVA_RUNTIME` environment variable on the Fabric peer. For example, `CORE_CHAINCODE_JAVA_RUNTIME=example/customJavaRuntime:latest`.

With Fabric v2 and later, an alternative chaincode builder can be configured on the Fabric peer. In this case the configured chaincode builder controls how chaincode is launched. See the [Fabric documentation](https://hyperledger-fabric.readthedocs.io/en/release-2.5/cc_launcher.html) for further details.

## Chaincode packaging

When using the `hyperledger/fabric-javaenv` Java chaincode Docker images, deployed chaincode is built as follows:

- If both Gradle and Maven files are present, Gradle is used.
- Gradle build files can be either Groovy or Kotlin.
- If Gradle or Maven wrappers are present, they will be used instead of the preinstalled Gradle or Maven versions.

Remember that when using the wrappers, code will be downloaded from the Internet. Keep this in mind for any installation with limited network access.

Alternatively, it is recommended to package prebuilt JAR files, including the smart contract and all dependencies. In this case, no Internet access is required when deploying Java chaincode.
