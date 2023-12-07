# Support and Compatibility for fabric-chaincode-java

Github is used for code base management, issues should reported in the [FABCJ](https://jira.hyperledger.org/projects/FABCJ/issues/) component in JIRA.


## Summary of Compatibility

This table shows the summary of the compatibility of the Java libraries, together with the JVM version they require and the Fabric Peer versions they can communicate with.

|                         | Fabric Peer v1.4 connectivity | Java 8 VM | Fabric Peer v2.2/2.5 Connectivity | Java 11 VM |
| ----------------------- | ----------------------------- | --------- | ----------------------------- | ---------- |
| Java libraries **v1.4** | Yes                           | Yes       | Yes                           | Yes        |
| Java libraries **v2.1** | Yes                           | No        | Yes                           | Yes        |
| Java libraries **v2.4** | Yes                           | No        | Yes                           | Yes        |
| Java libraries **v2.5** | Yes                           | No        | Yes                           | Yes        |


Testing is performed with 
 - Java v8: OpenJDK
 - Java v11: Eclipse Temurin (this has changed from OpenJDK)
 

By default a Fabric Peer v1.4 will create a Java 8 VM, and a Fabric Peer v2.x will create a Java 11 VM. Whilst is the default, the docker image used to host the chaincode and contracts can be altered.  Set the environment variable `CORE_CHAINCODE_JAVA_RUNTIME` on the peer to the name of the docker image. For example `CORE_CHAINCODE_JAVA_RUNTIME=example/customJavaRuntime:latest`

The Java Libraries will connect to the peer whilst running; this is referred to as 'Fabric Peer Connectivity' in the table. For example, whilst the Fabric Peer v1.4 will create a Java 8 environment, if a Java 11 environment was configured, the Java Libraries at v2.5 still function when connecting to the Fabric Peer v1.4.

## Compatibility

The key elements are:

- the version of the Fabric Contract Java libraries used
- the version of the JVM used to run the code
- When starting a chaincode container to run a Smart Contract the version of the runtime that is used is determined by these factors:

Fabric v1.4.2, and Fabric v2.5.x will, by default, start up docker image to host the chaincode and contracts. The version of the docker image used is defined by the version of Fabric in use.

With Fabric v2.1.0 and later, the chaincode container can be configured to be started by different chaincode builders, and not the Peer. In this case, the environment used is not in the control of Fabric.

The Java libraries are produced are `group: 'org.hyperledger.fabric-chaincode-java', name: 'fabric-chaincode-shim'`

### Supported JVMs

v1.4.x and v2.5.x Java Libraries are supported running in Java 11 with the x86_64 architecture. Later Java 11 versions are supported but are not tested.

v1.4.x Java Libraries are supported running in Java 8 with the x86_64 architecture. Later Java 8 versions are supported but are not tested.

Architecture Support: all docker images, JVMs, tools are tested under x86_64 ONLY


### Default Peer Runtime selection

When using Fabric v2.5, the default docker image that is used to run the Java chaincode is *eclipse-temurin:11.0.21_9-jdk*

With the default docker image used by Fabric v2.5, if the packaged Java code contains a build script or a wrapper for either Maven or Gradle, it will be built using Gradle 7.0 wrapper, or Maven 3.8.1 wrapper.  

    - If both Gradle and Maven files are present Gradle is used.  
    - Gradle build files can be groovy, or kotlin.  
    - If the Gradle or Maven wrappers are present, this will used in preference to the installed wrappers.
    
Remember that when using the wrappers, code will be downloaded from the internet. Keep this in mind for any installation with limited or no internet access.
    
Alternatively it is recommended to package prebuilt jar files, including the contract and all dependencies, in which case no build or Internet access is required when installing Java chaincode.

Please check the [Dockerfile](./fabric-chaincode-docker/Dockerfile) that is used for the environment to see exactly how these versions are installed. 

### Supported Runtime communication with the Peer

Subject to a suitable runtime environment, the 1.4 and 2.5 Java Libraries can used to communicate with Fabric Peers at 2.5 and previous LTS versions. The level of functionality that is implied by the Fabric version in use and channel capabilities.
