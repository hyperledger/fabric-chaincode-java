FROM hyperledger/fabric-baseimage:amd64-0.4.13
RUN apt-get update
RUN apt-get install zip -y
RUN curl  -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

# Install gradle and maven
RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install gradle 4.6
RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install maven 3.5.0

# Coping libs, scripts and sources
ADD build/distributions/ /root/

#Creating folders structure
RUN mkdir -p /root/chaincode-java/chaincode/src
RUN mkdir -p /root/chaincode-java/chaincode/build/out
RUN mkdir -p /chaincode/input
RUN mkdir -p /chaincode/output

#Making scripts runnable
RUN chmod +x /root/chaincode-java/start
RUN chmod +x /root/chaincode-java/build.sh

# Start build shim jars
WORKDIR /root/chaincode-java/shim-src
RUN source /root/.sdkman/bin/sdkman-init.sh; gradle clean

# Building protobuf jar and installing it to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos
RUN source /root/.sdkman/bin/sdkman-init.sh; gradle clean build install publishToMavenLocal
# Installing all jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos/build/publications/protosJar/
RUN source /root/.sdkman/bin/sdkman-init.sh; mvn -f pom-default.xml compile

# Building shim jar and installing it to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim
RUN source /root/.sdkman/bin/sdkman-init.sh; gradle clean build install publishToMavenLocal
# Installing all jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim/build/publications/shimJar/
RUN source /root/.sdkman/bin/sdkman-init.sh; mvn -f pom-default.xml compile

# Sanity check and maven/gradle plugin installs - compiling sample chaincode - gradle and maven
WORKDIR /root/chaincode-java/example-src/fabric-chaincode-example-gradle
RUN source /root/.sdkman/bin/sdkman-init.sh; gradle build shadowJar
WORKDIR /root/chaincode-java/example-src/fabric-chaincode-example-maven
RUN source /root/.sdkman/bin/sdkman-init.sh; mvn compile package
WORKDIR /root/chaincode-java

#Removing non-needed sources
RUN rm -rf example-src/*
RUN rm -rf shim-src
