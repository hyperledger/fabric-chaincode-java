# Support and Compatibility for fabric-chaincode-java

Github is used for code base management, issues should reported in the [FABCJ](https://jira.hyperledger.org/projects/FABCJ/issues/) component in JIRA.


## Summary of Compatibility

This table shows the summary of the compatibility of the Java libraries at versions 1.4 and 2.0, together with the JVM version they require and the Fabric Peer versions they can communicate with.

|                         | Fabric Peer v1.4 connectivity | Java 8 VM | Fabric Peer v2.0 Connectivity | Java 11 VM |
| ----------------------- | ----------------------------- | --------- | ----------------------------- | ---------- |
| Java libraries **v1.4** | Yes                           | Yes       | Yes                           | Yes        |
| Java libraries **v2.0** | Yes                           | No        | Yes                           | Yes        |

Testing is performed with 
 - Java v8: Openjdk version  1.8.0_222
 - Java v11: Openjdk version 11.04_11

By default a Fabric Peer v1.4 will create a Java 8 VM, and a Fabric Peer v2.0 will create a Java 11 VM. Whilst is the default, the docker image used to host the chaincode and contracts can be altered.  Set the environment variable `CORE_CHAINCODE_JAVA_RUNTIME` on the peer to the name of the docker image. For example `CORE_CHAINCODE_JAVA_RUNTIME=example/customJavaRuntime:latest`

The Java Libraries will connect to the peer whilst running; this is referred to as 'Fabric Peer Connectivity' in the table. For example, whilst the Fabric Peer v1.4 will create a Java 8 environment, if a Java 11 environment was configured, the Java Libraries at v2.0.0 still function when connecting to the Fabric Peer v1.4.

## Compatibility

The key elements are : 

- the version of the Fabric Contract Java libraries used
- the version of the JVM used to run the code
- When starting a chaincode container to run a Smart Contract the version of the runtime that is used is determined by these factors:

Fabric v1.4.2, and Fabric v2.0.0 will, by default, start up docker image to host the chaincode and contracts. The version of the docker image used is defined by the version of Fabric in use.

With Fabric v2.0.0, the chaincode container can be configured to be started by other means, and not the Peer. In this case, the environment used is not in the control of Fabric.

The Java libraries are produced are `group: 'org.hyperledger.fabric-chaincode-java', name: 'fabric-chaincode-shim'`

### Supported JVMs

v1.4.x and v2.0.0 Java Libraries are supported running in Java 11 with the x86_64 architecture. Later Java 11 versions are supported but are not tested.

v1.4.x Java Libraries are supported running in Java 8 with the  x86_64 architecture. Later Java 8 versions are supported but are not tested.

Architecture Support: all docker images, JVMs, tools are tested under x86_64 ONLY

### Default Peer Runtime selection

When using Fabric 2.0.0, the default docker image that is used to run the Java chaincode is *openjdk11:jdk-11.04_11-alpine*

With the default docker image used by Fabric 2.0.0. should the packaged Java code contains a maven or gradle build script, it will be built using Gradle 5.6.2, or Maven 3.6.2 (if both Gradle and Maven files are present Gradle is used.  Gradle build files can be groovy, or kotlin.  If the Gradle wrapper is present, this will used in preference to the installed version of Gradle)

### Supported Runtime communication with the Peer
 
Subject to a suitable runtime environment, the 1.4.4 and 2.0.0 Java Libraries can used to communicate with a Fabric 2.0.0 or 1.4.4 Peer - with the level of functionality that is implied by the Fabric version in use. 