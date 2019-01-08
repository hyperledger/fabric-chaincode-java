FROM openjdk:8-slim as builder
RUN apt-get update -y && apt-get upgrade -y && apt-get install curl -y && apt-get install zip -y

RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install gradle 4.6
RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install maven 3.5.0

FROM openjdk:8-slim as dependencies
RUN apt-get update -y && apt-get upgrade -y
RUN apt-get install wget -y

COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

SHELL ["/bin/bash", "-c"]
ENV PATH="/usr/bin/maven/bin:/usr/bin/maven/:/usr/bin/gradle:/usr/bin/gradle/bin:${PATH}"

# Coping libs, scripts and sources
ADD build/distributions/ /root/

#Creating folders structure
RUN mkdir -p /root/chaincode-java/chaincode/src
RUN mkdir -p /root/chaincode-java/chaincode/build/out

#Making scripts runnable
RUN chmod +x /root/chaincode-java/start
RUN chmod +x /root/chaincode-java/build.sh

# Start build shim jars
WORKDIR /root/chaincode-java/shim-src
RUN gradle clean

# Building protobuf jar and installing it to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos
RUN gradle clean build install publishToMavenLocal -x test
# Installing all jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos/build/publications/protosJar/
RUN mvn -f pom-default.xml compile

# Building shim jar and installing it to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim
RUN gradle clean build install publishToMavenLocal -x test
# Installing all jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim/build/publications/shimJar/
RUN mvn -f pom-default.xml compile

# Sanity check and maven/gradle plugin installs - compiling sample chaincode - gradle and maven
WORKDIR /root/chaincode-java/example-src/fabric-chaincode-example-gradle
RUN gradle build shadowJar
WORKDIR /root/chaincode-java/example-src/fabric-chaincode-example-maven
RUN mvn compile package
WORKDIR /root/chaincode-java

# Adding shim 1.3.0 jar
WORKDIR /tmp
RUN wget https://nexus.hyperledger.org/content/repositories/releases/org/hyperledger/fabric-chaincode-java/fabric-chaincode-shim/1.3.0/fabric-chaincode-shim-1.3.0.pom
RUN wget https://nexus.hyperledger.org/content/repositories/releases/org/hyperledger/fabric-chaincode-java/fabric-chaincode-shim/1.3.0/fabric-chaincode-shim-1.3.0.jar
RUN wget https://nexus.hyperledger.org/content/repositories/releases/org/hyperledger/fabric-chaincode-java/fabric-chaincode-protos/1.3.0/fabric-chaincode-protos-1.3.0.pom
RUN wget https://nexus.hyperledger.org/content/repositories/releases/org/hyperledger/fabric-chaincode-java/fabric-chaincode-protos/1.3.0/fabric-chaincode-protos-1.3.0.jar
RUN mvn install::install-file -Dfile=fabric-chaincode-protos-1.3.0.jar -DpomFile=fabric-chaincode-protos-1.3.0.pom
RUN mvn install::install-file -Dfile=fabric-chaincode-shim-1.3.0.jar -DpomFile=fabric-chaincode-shim-1.3.0.pom

#Removing non-needed sources
WORKDIR /root/chaincode-javaRUN rm -rf example-src
RUN rm -rf shim-src

# Creating final javaenv image which will include all required
# dependencies to build and compile java chaincode
FROM openjdk:8-slim
RUN apt-get update -y && apt-get upgrade -y
COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

SHELL ["/bin/bash", "-c"]
ENV PATH="/usr/bin/maven/bin:/usr/bin/maven/:/usr/bin/gradle:/usr/bin/gradle/bin:${PATH}"

COPY --from=dependencies /root/chaincode-java /root/chaincode-java
COPY --from=dependencies /root/.m2 /root/.m2

RUN mkdir -p /chaincode/input
RUN mkdir -p /chaincode/output

WORKDIR /root/chaincode-java
