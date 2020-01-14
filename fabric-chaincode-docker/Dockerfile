FROM adoptopenjdk/openjdk11:jdk-11.0.4_11-alpine as builder
RUN apk add --no-cache bash curl zip

RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install gradle 5.6.2
RUN source /root/.sdkman/bin/sdkman-init.sh; sdk install maven 3.6.2

FROM adoptopenjdk/openjdk11:jdk-11.0.4_11-alpine as dependencies
RUN apk add --no-cache bash wget

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

# Build protos and shim jar and installing them to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src
RUN gradle \
    clean \
    fabric-chaincode-protos:build \
    fabric-chaincode-protos:install \
    fabric-chaincode-protos:publishToMavenLocal \
    fabric-chaincode-shim:build \
    fabric-chaincode-shim:install \
    fabric-chaincode-shim:publishToMavenLocal \
    -x javadoc \
    -x test \
    -x checkstyleMain -x checkstyleTest -x dependencyCheckAnalyze

# Installing all protos jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-protos/build/publications/protosJar/
RUN mvn -f pom-default.xml compile

# Installing all shim jar dependencies to maven local
WORKDIR /root/chaincode-java/shim-src/fabric-chaincode-shim/build/publications/shimJar/
RUN mvn -f pom-default.xml compile

#Removing non-needed sources
WORKDIR /root/chaincode-java
RUN rm -rf shim-src

# Creating final javaenv image which will include all required
# dependencies to build and compile java chaincode
FROM adoptopenjdk/openjdk11:jdk-11.0.4_11-alpine
RUN apk add --no-cache bash
COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

SHELL ["/bin/bash", "-c"]
ENV PATH="/usr/bin/maven/bin:/usr/bin/maven/:/usr/bin/gradle:/usr/bin/gradle/bin:${PATH}"

COPY --from=dependencies /root/chaincode-java /root/chaincode-java
COPY --from=dependencies /root/.m2 /root/.m2

RUN mkdir -p /chaincode/input
RUN mkdir -p /chaincode/output

WORKDIR /root/chaincode-java
