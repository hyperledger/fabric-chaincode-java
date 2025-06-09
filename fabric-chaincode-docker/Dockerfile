ARG JAVA_IMAGE=eclipse-temurin:21-jdk

FROM ${JAVA_IMAGE} AS builder
ENV DEBIAN_FRONTEND=noninteractive

# Build tools
RUN apt-get update \
    && apt-get -y install zip unzip \       
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

RUN source /root/.sdkman/bin/sdkman-init.sh \
    && sdk install gradle 8.14.2 \
    && sdk install maven 3.9.10

FROM ${JAVA_IMAGE} AS dependencies

COPY --from=builder /root/.sdkman/candidates/gradle/current /usr/bin/gradle
COPY --from=builder /root/.sdkman/candidates/maven/current /usr/bin/maven

SHELL ["/bin/bash", "-c"]
ENV PATH="/usr/bin/maven/bin:/usr/bin/maven/:/usr/bin/gradle:/usr/bin/gradle/bin:${PATH}"

# Coping libs, scripts and sources
ADD build/distributions/ /root/

#Creating folders structure
RUN mkdir -p /root/chaincode-java/chaincode/src /root/chaincode-java/chaincode/build/out

#Making scripts runnable
RUN chmod +x /root/chaincode-java/start /root/chaincode-java/build.sh

# Build protos and shim jar and installing them to maven local and gradle cache
WORKDIR /root/chaincode-java/shim-src
RUN gradle \
    clean \
    fabric-chaincode-shim:build \
    fabric-chaincode-shim:publishToMavenLocal \
    -x javadoc \
    -x test \
    -x pmdMain \
    -x pmdTest \
    -x spotlessCheck

WORKDIR /root/chaincode-java 
# Run the Gradle and Maven commands to generate the wrapper variants
# of each tool
#Gradle doesn't run without settings.gradle file, so create one
RUN touch settings.gradle \
    && gradle wrapper \
    && ./gradlew --version \
    && mvn -N wrapper:wrapper

# Creating final javaenv image which will include all required
# dependencies to build and compile java chaincode
FROM ${JAVA_IMAGE}

RUN apt-get update \
    && apt-get -y install zip unzip \       
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /chaincode/input \
    && mkdir -p /chaincode/output

SHELL ["/bin/bash", "-c"]

# Copy setup scripts, and the cached dependencies
COPY --from=dependencies /root/chaincode-java /root/chaincode-java
COPY --from=dependencies /root/.gradle /root/.gradle
COPY --from=dependencies /root/.m2 /root/.m2

WORKDIR /root/chaincode-java
