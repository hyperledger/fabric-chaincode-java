FROM hyperledger/fabric-baseimage:amd64-0.4.14 as builder
RUN apt-get update
RUN apt-get install zip -y
RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install gradle 4.6
RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install maven 3.5.0

FROM hyperledger/fabric-baseimage:amd64-0.4.14 as dependencies
COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

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
RUN gradle clean build install publishToMavenLocal
# Installing all jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos/build/publications/protosJar/
RUN mvn -f pom-default.xml compile

# Building shim jar and installing it to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim
RUN gradle clean build install publishToMavenLocal
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
RUN source /root/.sdkman/bin/sdkman-init.sh; mvn dependency:get -DgroupId=org.hyperledger.fabric-chaincode-java -DartifactId=fabric-chaincode-shim -Dversion=1.3.0 -DremoteRepositoriescentral::default::https://nexus.hyperledger.org/content/repositories/releases

#Removing non-needed sources
RUN rm -rf example-src
RUN rm -rf shim-src

# Creating final javaenv image which will include all required
# dependencies to build and compile java chaincode
FROM hyperledger/fabric-baseimage:amd64-0.4.14
COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

ENV PATH="/usr/bin/maven/bin:/usr/bin/maven/:/usr/bin/gradle:/usr/bin/gradle/bin:${PATH}"

COPY --from=dependencies /root/chaincode-java /root/chaincode-java
COPY --from=dependencies /root/.m2 /root/.m2

RUN mkdir -p /chaincode/input
RUN mkdir -p /chaincode/output

WORKDIR /root/chaincode-java
