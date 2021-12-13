## v2.4.1
Mon Dec 13 11:50:31 GMT 2021

* [bf054cb0](https://github.com/hyperledger/fabric-chaincode-java/commit/bf054cb0) Add a brief example for the -as-a-service
* [f9ada8f5](https://github.com/hyperledger/fabric-chaincode-java/commit/f9ada8f5) Chaincode-as-a-service main bootstrap method

## 2.4.0
Fri Nov 26 08:21:40 GMT 2021

Release 2.4.0 

## v2.4.0-beta
Fri 13 Aug 2021 16:43:31 CEST

* [292c4ebe](https://github.com/hyperledger/fabric-chaincode-java/commit/292c4ebe) Update the build.gradle for publishing

## v2.3.1
Wed 21 Jul 2021 11:20:03 BST

* [6be7a724](https://github.com/hyperledger/fabric-chaincode-java/commit/6be7a724) Integration tests extended
* [66e25ea9](https://github.com/hyperledger/fabric-chaincode-java/commit/66e25ea9) NettyGrpcServer -- support mutual TLS
* [1128e7b2](https://github.com/hyperledger/fabric-chaincode-java/commit/1128e7b2) NettyGrpcServer: logger->LOGGER
* [b7b4ef12](https://github.com/hyperledger/fabric-chaincode-java/commit/b7b4ef12) NettyGrpcServer -- reformat code
* [3a98fc5e](https://github.com/hyperledger/fabric-chaincode-java/commit/3a98fc5e) NettyGrpcServer -- configure ALPN
* [63c12ffa](https://github.com/hyperledger/fabric-chaincode-java/commit/63c12ffa) NettyGrpcServerTest -- improve startAndStopTlsPassword, startAndStopTlsWithoutPassword test cases
* [5046eb79](https://github.com/hyperledger/fabric-chaincode-java/commit/5046eb79) Bump logback-classic in /examples/fabric-contract-example-maven
* [ab2a166d](https://github.com/hyperledger/fabric-chaincode-java/commit/ab2a166d) added tests
* [25706938](https://github.com/hyperledger/fabric-chaincode-java/commit/25706938) Add additional contracts for deployment test
* [e096d4f7](https://github.com/hyperledger/fabric-chaincode-java/commit/e096d4f7) Review ideas
* [7645bb9d](https://github.com/hyperledger/fabric-chaincode-java/commit/7645bb9d) Move to use Maven Wrapper
* [d212e2a7](https://github.com/hyperledger/fabric-chaincode-java/commit/d212e2a7) Upgrade gradle to v7
* [f1d6b7da](https://github.com/hyperledger/fabric-chaincode-java/commit/f1d6b7da) Gradle wrapper updated to the latest version
* [3f190ef5](https://github.com/hyperledger/fabric-chaincode-java/commit/3f190ef5) Transaction metadata tags updated
* [0d0c9280](https://github.com/hyperledger/fabric-chaincode-java/commit/0d0c9280) Update "master" branch references to "main"
* [ca69f15d](https://github.com/hyperledger/fabric-chaincode-java/commit/ca69f15d) Fix link in SECURITY.md
* [4c6cbf47](https://github.com/hyperledger/fabric-chaincode-java/commit/4c6cbf47) Correct condition
* [76c7fe45](https://github.com/hyperledger/fabric-chaincode-java/commit/76c7fe45) Cleanup files
* [70bdd194](https://github.com/hyperledger/fabric-chaincode-java/commit/70bdd194) Add in publishing of the nightly master branch drivers
* [b51eac20](https://github.com/hyperledger/fabric-chaincode-java/commit/b51eac20) Change FABRIC_VERSION to latest tag
* [28dfb24b](https://github.com/hyperledger/fabric-chaincode-java/commit/28dfb24b) fix fabric-contract-example-maven/pom.xml
* [ff48941c](https://github.com/hyperledger/fabric-chaincode-java/commit/ff48941c) Logging Tests Reset Correctly
* [cc31ae01](https://github.com/hyperledger/fabric-chaincode-java/commit/cc31ae01) [FABCJ-214](https://jira.hyperledger.org/browse/FABCJ-214) - Java chaincode gRPC server
* [06f72193](https://github.com/hyperledger/fabric-chaincode-java/commit/06f72193) Fix "build shadowJar" error
* [278e9f8e](https://github.com/hyperledger/fabric-chaincode-java/commit/278e9f8e) [FABCJ-187](https://jira.hyperledger.org/browse/FABCJ-187) Add max inbound msg size configuration
* [bf4f30aa](https://github.com/hyperledger/fabric-chaincode-java/commit/bf4f30aa) Fix javadoc build
* [9e403b6d](https://github.com/hyperledger/fabric-chaincode-java/commit/9e403b6d) Fix JavaDoc link to Compatibility documentation
* [57678eea](https://github.com/hyperledger/fabric-chaincode-java/commit/57678eea) Bump version to 2.3.1
* [91e6001d](https://github.com/hyperledger/fabric-chaincode-java/commit/91e6001d) [FABCJ-290](https://jira.hyperledger.org/browse/FABCJ-290) Add release guide
* [0deb7e0d](https://github.com/hyperledger/fabric-chaincode-java/commit/0deb7e0d) [FABCJ-293](https://jira.hyperledger.org/browse/FABCJ-293) Remove gradle from image
* [2dfe17c1](https://github.com/hyperledger/fabric-chaincode-java/commit/2dfe17c1) [FABCJ-291](https://jira.hyperledger.org/browse/FABCJ-291) Startkey needs additional checks For the open ended query, the start and empty keys are empty from the chaincode. However, in the shim, if the start key is an empty key, it is replaced with 0x01  which is nothing but a namespace for the non-composite key.
* [21d81193](https://github.com/hyperledger/fabric-chaincode-java/commit/21d81193) Update doc links
* [78ed0157](https://github.com/hyperledger/fabric-chaincode-java/commit/78ed0157) [FABCJ-288](https://jira.hyperledger.org/browse/FABCJ-288) fix: simple key end of range
* [90f6c3c2](https://github.com/hyperledger/fabric-chaincode-java/commit/90f6c3c2) Update dependencies
* [526d1cc8](https://github.com/hyperledger/fabric-chaincode-java/commit/526d1cc8) [FABCJ-284](https://jira.hyperledger.org/browse/FABCJ-284) Broken link
* [7f722053](https://github.com/hyperledger/fabric-chaincode-java/commit/7f722053) force rebuild
* [9a42effb](https://github.com/hyperledger/fabric-chaincode-java/commit/9a42effb) [FABCJ-285](https://jira.hyperledger.org/browse/FABCJ-285) Remove incorrect log point
* [c2367768](https://github.com/hyperledger/fabric-chaincode-java/commit/c2367768) Clean up Fabric Version Methodology
* [88da28f6](https://github.com/hyperledger/fabric-chaincode-java/commit/88da28f6) [FAB-17777](https://jira.hyperledger.org/browse/FAB-17777) Create basic settings.yaml
* [a6b00f2d](https://github.com/hyperledger/fabric-chaincode-java/commit/a6b00f2d) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Update javadoc 2.x link
* [96fa4d6e](https://github.com/hyperledger/fabric-chaincode-java/commit/96fa4d6e) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Update docs to handle new branch
* [ff031f2d](https://github.com/hyperledger/fabric-chaincode-java/commit/ff031f2d) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Bump version number to 3.0.0 - in order to not contradict release-2.x branch
* [35a5159d](https://github.com/hyperledger/fabric-chaincode-java/commit/35a5159d) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Update docker image version to 2.1
* [67af7977](https://github.com/hyperledger/fabric-chaincode-java/commit/67af7977) [FABCJ-280](https://jira.hyperledger.org/browse/FABCJ-280) Copy chaincode into temporary directory before building
* [e69168cb](https://github.com/hyperledger/fabric-chaincode-java/commit/e69168cb) [FABCJ-276](https://jira.hyperledger.org/browse/FABCJ-276) Access localmspid
* [cc673036](https://github.com/hyperledger/fabric-chaincode-java/commit/cc673036) [FABCN-359](https://jira.hyperledger.org/browse/FABCN-359) Ledger Class
* [f0689360](https://github.com/hyperledger/fabric-chaincode-java/commit/f0689360) [FABCJ-273](https://jira.hyperledger.org/browse/FABCJ-273) Show release-2.0 Javadoc
* [364cae9d](https://github.com/hyperledger/fabric-chaincode-java/commit/364cae9d) [FABCJ-269](https://jira.hyperledger.org/browse/FABCJ-269) Compatibility Matrix
* [89ffb592](https://github.com/hyperledger/fabric-chaincode-java/commit/89ffb592) [FABCJ-273](https://jira.hyperledger.org/browse/FABCJ-273) Prepare next release v2.1.0
* [6c1cdba0](https://github.com/hyperledger/fabric-chaincode-java/commit/6c1cdba0) Java chaincode gRPC server for run external chaincode add NettyGrpcServer with method /Connect for start chat with peer without TLS add example external chaincode

## v2.3.0
Tue  3 Nov 2020 14:20:11 GMT

* [081d5c8](https://github.com/hyperledger/fabric-chaincode-java/commit/081d5c8) Bump version to 2.2.2
* [b494824](https://github.com/hyperledger/fabric-chaincode-java/commit/b494824) [FABCJ-290](https://jira.hyperledger.org/browse/FABCJ-290) Add release guide

## v2.2.1
Wed  7 Oct 2020 16:21:53 BST

* [cef231c](https://github.com/hyperledger/fabric-chaincode-java/commit/cef231c) [FABCJ-293](https://jira.hyperledger.org/browse/FABCJ-293) Remove gradle from image
* [d2643ef](https://github.com/hyperledger/fabric-chaincode-java/commit/d2643ef) Minor Performance Improvements
* [703558c](https://github.com/hyperledger/fabric-chaincode-java/commit/703558c) [FABCJ-291](https://jira.hyperledger.org/browse/FABCJ-291) Startkey needs additional checks For the open ended query, the start and empty keys are empty from the chaincode. However, in the shim, if the start key is an empty key, it is replaced with 0x01  which is nothing but a namespace for the non-composite key.
* [f35ae08](https://github.com/hyperledger/fabric-chaincode-java/commit/f35ae08) Fix tagging of fabric-javaenv image
* [cb31d36](https://github.com/hyperledger/fabric-chaincode-java/commit/cb31d36) Bump version to 2.2.1

## v2.2.0
Thu  2 Jul 11:28:13 BST 2020

* [32c8201](https://github.com/hyperledger/fabric-chaincode-java/commit/32c8201) [FABCJ-289](https://jira.hyperledger.org/browse/FABCJ-289) release: 2.2.0 LTS
* [0948234](https://github.com/hyperledger/fabric-chaincode-java/commit/0948234) [FABCJ-288](https://jira.hyperledger.org/browse/FABCJ-288) fix: simple key end of range
* [8b06be2](https://github.com/hyperledger/fabric-chaincode-java/commit/8b06be2) [FABCJ-286](https://jira.hyperledger.org/browse/FABCJ-286) Prepare 2.1.2

## v2.1.1
Mon 18 May 09:09:24 BST 2020

* [f0f958e](https://github.com/hyperledger/fabric-chaincode-java/commit/f0f958e) [FABCJ-284](https://jira.hyperledger.org/browse/FABCJ-284) Broken docs link
* [b89c464](https://github.com/hyperledger/fabric-chaincode-java/commit/b89c464) [FABCJ-285](https://jira.hyperledger.org/browse/FABCJ-285) Remove incorrect log point
* [93ff6bb](https://github.com/hyperledger/fabric-chaincode-java/commit/93ff6bb) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Bump version number to 2.1.1

## v2.1.0
Thu  9 Apr 2020 14:13:36 BST

* [72e6f78](https://github.com/hyperledger/fabric-chaincode-java/commit/72e6f78) [FABCJ-283](https://jira.hyperledger.org/browse/FABCJ-283) Update docker image version
* [54606f8](https://github.com/hyperledger/fabric-chaincode-java/commit/54606f8) [FABCJ-269](https://jira.hyperledger.org/browse/FABCJ-269) Compatibility Matrix

## v2.0.1
Wed  4 Mar 16:38:58 GMT 2020

* [8cb6a25](https://github.com/hyperledger/fabric-chaincode-java/commit/8cb6a25) [FAB-16136](https://jira.hyperledger.org/browse/FAB-16136) Do not run tests in chaincode container
* [3710641](https://github.com/hyperledger/fabric-chaincode-java/commit/3710641) [FABCJ-280](https://jira.hyperledger.org/browse/FABCJ-280) Copy chaincode into temporary directory before building
* [a25a7d6](https://github.com/hyperledger/fabric-chaincode-java/commit/a25a7d6) [FABCJ-276](https://jira.hyperledger.org/browse/FABCJ-276) Access localmspid

## v2.0.0
Fri 24 Jan 10:26:03 GMT 2020

* [659a1c4](https://github.com/hyperledger/fabric-chaincode-java/commit/659a1c4) Port fixes from master branch for Coverage & typos
* [9cf1e6a](https://github.com/hyperledger/fabric-chaincode-java/commit/9cf1e6a) [FABCI-482](https://jira.hyperledger.org/browse/FABCI-482) Update Nexus URL's to Artifactory
* [7ab7145](https://github.com/hyperledger/fabric-chaincode-java/commit/7ab7145) Fix typos in README.md
* [67fdc40](https://github.com/hyperledger/fabric-chaincode-java/commit/67fdc40) [FABCJ-259](https://jira.hyperledger.org/browse/FABCJ-259) Pagination Fix
* [1609425](https://github.com/hyperledger/fabric-chaincode-java/commit/1609425) Update maintainers list
* [1a45e3d](https://github.com/hyperledger/fabric-chaincode-java/commit/1a45e3d) [FABCJ-95](https://jira.hyperledger.org/browse/FABCJ-95) Checkstyle


## v2.0.0-beta
Thu 12 Dec 12:45:44 GMT 2019

* [4a13009](https://github.com/hyperledger/fabric-chaincode-java/commit/4a13009) Add OWASP dependency checks to build
* [44c96d7](https://github.com/hyperledger/fabric-chaincode-java/commit/44c96d7) FABCJ-258 Add latest image tag in AZP pipeline
* [e1d53bf](https://github.com/hyperledger/fabric-chaincode-java/commit/e1d53bf) Remove -SNAPSHOT from package version variable
* [fedc2ef](https://github.com/hyperledger/fabric-chaincode-java/commit/fedc2ef) Improve integration test reliability
* [6fd5507](https://github.com/hyperledger/fabric-chaincode-java/commit/6fd5507) [FABCJ-257] Rationalize examples
* [5d3cd53](https://github.com/hyperledger/fabric-chaincode-java/commit/5d3cd53) [FABCJ-160] Char support
* [e32e404](https://github.com/hyperledger/fabric-chaincode-java/commit/e32e404) [FABCJ-183] Extra properties appear in JSON from Java Objects
* [7f3c4e3](https://github.com/hyperledger/fabric-chaincode-java/commit/7f3c4e3) [FAB-17138](https://jira.hyperledger.org/browse/FAB-17138) Publish docker images to nexus
* [b52d26a](https://github.com/hyperledger/fabric-chaincode-java/commit/b52d26a) [FAB-15634](https://jira.hyperledger.org/browse/FAB-15634) Annotations for Serializer
* [207bd94](https://github.com/hyperledger/fabric-chaincode-java/commit/207bd94) [FAB-17100](https://jira.hyperledger.org/browse/FAB-17100) Default Thread Pool not set correctly
* [f50a4ed](https://github.com/hyperledger/fabric-chaincode-java/commit/f50a4ed) [FAB-16712](https://jira.hyperledger.org/browse/FAB-16712) Disable stalebot
* [ee8a1f4](https://github.com/hyperledger/fabric-chaincode-java/commit/ee8a1f4) Update javadoc location in readme
* [3951dca](https://github.com/hyperledger/fabric-chaincode-java/commit/3951dca) Fix git user email/name config in azure pipeline
* [55ad1b9](https://github.com/hyperledger/fabric-chaincode-java/commit/55ad1b9) Update javadoc build to use Hyperledger Bot
* [4680efc](https://github.com/hyperledger/fabric-chaincode-java/commit/4680efc) Publish javadoc
* [d4981a3](https://github.com/hyperledger/fabric-chaincode-java/commit/d4981a3) Fix the name of the docker image
* [6553ffc](https://github.com/hyperledger/fabric-chaincode-java/commit/6553ffc) Update readme
* [3b029a8](https://github.com/hyperledger/fabric-chaincode-java/commit/3b029a8) Fix the name of the docker image
* [d01eeff](https://github.com/hyperledger/fabric-chaincode-java/commit/d01eeff) Add artifacts to build result
* [3c2c27c](https://github.com/hyperledger/fabric-chaincode-java/commit/3c2c27c) [FAB-16712](https://jira.hyperledger.org/browse/FAB-16712) Update contributing guide
* [d13e0c0](https://github.com/hyperledger/fabric-chaincode-java/commit/d13e0c0) Next steps in publishing
* [75a0bed](https://github.com/hyperledger/fabric-chaincode-java/commit/75a0bed) Next steps in publishing
* [fd2e21a](https://github.com/hyperledger/fabric-chaincode-java/commit/fd2e21a) Next steps in publishing
* [5618de6](https://github.com/hyperledger/fabric-chaincode-java/commit/5618de6) Push the snapshot docker images to nexus
* [5a11c6e](https://github.com/hyperledger/fabric-chaincode-java/commit/5a11c6e) Push the snapshot docker images to nexus
* [4eff463](https://github.com/hyperledger/fabric-chaincode-java/commit/4eff463) [FAB-16315](https://jira.hyperledger.org/browse/FAB-16315) Improved Load Ability
* [11a26ce](https://github.com/hyperledger/fabric-chaincode-java/commit/11a26ce) Fix Azure Pipelines
* [7b3e509](https://github.com/hyperledger/fabric-chaincode-java/commit/7b3e509) [FAB-16711](https://jira.hyperledger.org/browse/FAB-16711) Azure pipelines
* [a3304ec](https://github.com/hyperledger/fabric-chaincode-java/commit/a3304ec) [FAB-16707](https://jira.hyperledger.org/browse/FAB-16707) Remove Gradle warnings
* [af3dec0](https://github.com/hyperledger/fabric-chaincode-java/commit/af3dec0) [IN-68] Add default GitHub SECURITY policy
* [88b9397](https://github.com/hyperledger/fabric-chaincode-java/commit/88b9397) [FAB-16680](https://jira.hyperledger.org/browse/FAB-16680) Fix cloudflare error on jitpack.io
* [f6076eb](https://github.com/hyperledger/fabric-chaincode-java/commit/f6076eb) [FAB-16669](https://jira.hyperledger.org/browse/FAB-16669) WIP: Use native Java 11 ALPN support
* [5520053](https://github.com/hyperledger/fabric-chaincode-java/commit/5520053) [FAB-16669](https://jira.hyperledger.org/browse/FAB-16669) Update gRPC and Protocol Buffers code
* [abff28f](https://github.com/hyperledger/fabric-chaincode-java/commit/abff28f) [FAB-16655](https://jira.hyperledger.org/browse/FAB-16655) Warnings as errors for Java chaincode docs
* [50e75bf](https://github.com/hyperledger/fabric-chaincode-java/commit/50e75bf) [FAB-16655](https://jira.hyperledger.org/browse/FAB-16655) Warnings as errors for Java chaincode
* [d79f5a6](https://github.com/hyperledger/fabric-chaincode-java/commit/d79f5a6) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Remove tests from Docker image build
* [35d5884](https://github.com/hyperledger/fabric-chaincode-java/commit/35d5884) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Various chaincode deployment updates
* [12b5243](https://github.com/hyperledger/fabric-chaincode-java/commit/12b5243) [FAB-16493](https://jira.hyperledger.org/browse/FAB-16493) Fixed gradle build on windows
* [2f6be19](https://github.com/hyperledger/fabric-chaincode-java/commit/2f6be19) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Remove cglib dependency
* [56c533e](https://github.com/hyperledger/fabric-chaincode-java/commit/56c533e) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Upgrade Docker image to Java 11
* [5dbbea7](https://github.com/hyperledger/fabric-chaincode-java/commit/5dbbea7) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Replace org.reflections with classgraph
* [059d043](https://github.com/hyperledger/fabric-chaincode-java/commit/059d043) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Add javax.xml.bind dependency for Java 11
* [cbe663b](https://github.com/hyperledger/fabric-chaincode-java/commit/cbe663b) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Upgrade to Gradle 5.6.2 and Maven 3.6.2
* [887153c](https://github.com/hyperledger/fabric-chaincode-java/commit/887153c) [FAB-6415](https://jira.hyperledger.org/browse/FAB-6415) Add javax.annotation dependency for Java 11
* [66e9079](https://github.com/hyperledger/fabric-chaincode-java/commit/66e9079) [FAB-16489](https://jira.hyperledger.org/browse/FAB-16489) Add CODEOWNERS
* [182c050](https://github.com/hyperledger/fabric-chaincode-java/commit/182c050) [FAB-15507](https://jira.hyperledger.org/browse/FAB-15507) Add doc
* [1a38b84](https://github.com/hyperledger/fabric-chaincode-java/commit/1a38b84) [FABN-1320] Remove sed from git_tag.sh script
* [25ed6c1](https://github.com/hyperledger/fabric-chaincode-java/commit/25ed6c1) [FABN-1320] Fix git_tag.sh
* [f47f601](https://github.com/hyperledger/fabric-chaincode-java/commit/f47f601) [FAB-15929](https://jira.hyperledger.org/browse/FAB-15929) Add getPrivateDataHash support
* [4371d07](https://github.com/hyperledger/fabric-chaincode-java/commit/4371d07) [FAB-16217](https://jira.hyperledger.org/browse/FAB-16217) Do not load JSON Schema schema from network
* [44d76f4](https://github.com/hyperledger/fabric-chaincode-java/commit/44d76f4) [FAB-15895](https://jira.hyperledger.org/browse/FAB-15895) Added client identity to context
* [9169e14](https://github.com/hyperledger/fabric-chaincode-java/commit/9169e14) New maintainer application
* [12309b5](https://github.com/hyperledger/fabric-chaincode-java/commit/12309b5) [FAB-16091](https://jira.hyperledger.org/browse/FAB-16091) Handle invalid contract name
* [e8ca970](https://github.com/hyperledger/fabric-chaincode-java/commit/e8ca970) [FAB-15647](https://jira.hyperledger.org/browse/FAB-15647) Fix markup of README
* [360d75a](https://github.com/hyperledger/fabric-chaincode-java/commit/360d75a) [FAB-15883](https://jira.hyperledger.org/browse/FAB-15883) Removed swagger annotations
* [25c5be6](https://github.com/hyperledger/fabric-chaincode-java/commit/25c5be6) [FAB-15615](https://jira.hyperledger.org/browse/FAB-15615) Add ChaincodeException support
* [9077581](https://github.com/hyperledger/fabric-chaincode-java/commit/9077581) [FAB-15823](https://jira.hyperledger.org/browse/FAB-15823) Fix getStringPayload npe
* [e163d3f](https://github.com/hyperledger/fabric-chaincode-java/commit/e163d3f) [FAB-15743](https://jira.hyperledger.org/browse/FAB-15743) Align Context
* [5ad6ed8](https://github.com/hyperledger/fabric-chaincode-java/commit/5ad6ed8) [FAB-15507](https://jira.hyperledger.org/browse/FAB-15507) Exclude unnecessary contract javadoc
* [9cc6553](https://github.com/hyperledger/fabric-chaincode-java/commit/9cc6553) [FAB-15720](https://jira.hyperledger.org/browse/FAB-15720) prevent duplicate transactions
* [f87de8e](https://github.com/hyperledger/fabric-chaincode-java/commit/f87de8e) [FAB-15632](https://jira.hyperledger.org/browse/FAB-15632) Add name element to transaction annotation
* [d726175](https://github.com/hyperledger/fabric-chaincode-java/commit/d726175) [FAB-13803](https://jira.hyperledger.org/browse/FAB-13803) Parameter Marshalling
* [72dc716](https://github.com/hyperledger/fabric-chaincode-java/commit/72dc716) [FAB-15214](https://jira.hyperledger.org/browse/FAB-15214) Remove init annotation
* [509bfbf](https://github.com/hyperledger/fabric-chaincode-java/commit/509bfbf) [FAB-13802](https://jira.hyperledger.org/browse/FAB-13802) Metadata Support
* [0467032](https://github.com/hyperledger/fabric-chaincode-java/commit/0467032) Application to be consider for maintainer of this repo
* [526f36d](https://github.com/hyperledger/fabric-chaincode-java/commit/526f36d) [FAB-13802](https://jira.hyperledger.org/browse/FAB-13802) Return types update
* [1662390](https://github.com/hyperledger/fabric-chaincode-java/commit/1662390) [FAB-13912](https://jira.hyperledger.org/browse/FAB-13912) Implement new interfaces
* [bd1c269](https://github.com/hyperledger/fabric-chaincode-java/commit/bd1c269) [FAB-14961](https://jira.hyperledger.org/browse/FAB-14961) Update to the FAQ
* [dc02c3b](https://github.com/hyperledger/fabric-chaincode-java/commit/dc02c3b) [FAB-14995](https://jira.hyperledger.org/browse/FAB-14995) Prepare for next release 

## v2.0.0-alpha
Sun Apr  9 15:37:09 IDT 2019

* [b62740c](https://github.com/hyperledger/fabric-chaincode-java/commit/b62740c) [FAB-13798](https://jira.hyperledger.org/browse/FAB-13798) New interfaces
* [8235149](https://github.com/hyperledger/fabric-chaincode-java/commit/8235149) [FAB-13795](https://jira.hyperledger.org/browse/FAB-13795) Contract definition
* [762a955](https://github.com/hyperledger/fabric-chaincode-java/commit/762a955) [FAB-13800](https://jira.hyperledger.org/browse/FAB-13800) Transaction Context
* [351acb5](https://github.com/hyperledger/fabric-chaincode-java/commit/351acb5) [FAB-13794](https://jira.hyperledger.org/browse/FAB-13794) New Annotations
* [3e13a57](https://github.com/hyperledger/fabric-chaincode-java/commit/3e13a57) [FAB-12960](https://jira.hyperledger.org/browse/FAB-12960) Adding cc without main method
* [40bf2c1](https://github.com/hyperledger/fabric-chaincode-java/commit/40bf2c1) [FAB-14533](https://jira.hyperledger.org/browse/FAB-14533) Updating integration tests
* [b17d8a2](https://github.com/hyperledger/fabric-chaincode-java/commit/b17d8a2) [FAB-12504](https://jira.hyperledger.org/browse/FAB-12504) Added maven integration test.
* [0e76b72](https://github.com/hyperledger/fabric-chaincode-java/commit/0e76b72) [FAB-14522](https://jira.hyperledger.org/browse/FAB-14522) README update: sdk compatibility
* [e5d3e40](https://github.com/hyperledger/fabric-chaincode-java/commit/e5d3e40) [FAB-14460](https://jira.hyperledger.org/browse/FAB-14460) Better chaincode source check
* [0adaf35](https://github.com/hyperledger/fabric-chaincode-java/commit/0adaf35) [FAB-13490](https://jira.hyperledger.org/browse/FAB-13490) javaenv image based on openjdk:slim-8
* [e33b4bd](https://github.com/hyperledger/fabric-chaincode-java/commit/e33b4bd) [FAB-13672](https://jira.hyperledger.org/browse/FAB-13672) stop using deprecated left closure
* [c3e342a](https://github.com/hyperledger/fabric-chaincode-java/commit/c3e342a) [FAB-13097](https://jira.hyperledger.org/browse/FAB-13097) Getting correct jar
* [cc76d2f](https://github.com/hyperledger/fabric-chaincode-java/commit/cc76d2f) [FAB-13097](https://jira.hyperledger.org/browse/FAB-13097) Adding shim 1.3.0 to docker image
* [b22858b](https://github.com/hyperledger/fabric-chaincode-java/commit/b22858b) Configure Stale ProBot
* [1279d2c](https://github.com/hyperledger/fabric-chaincode-java/commit/1279d2c) [FAB-13436](https://jira.hyperledger.org/browse/FAB-13436) multistage docker build
* [035a7d2](https://github.com/hyperledger/fabric-chaincode-java/commit/035a7d2) [FAB-13243](https://jira.hyperledger.org/browse/FAB-13243) fix javaenv docker build tag
* [ad89bbb](https://github.com/hyperledger/fabric-chaincode-java/commit/ad89bbb) [FAB-13248](https://jira.hyperledger.org/browse/FAB-13248) Update dependencies for sonatype
* [cea4db0](https://github.com/hyperledger/fabric-chaincode-java/commit/cea4db0) [FAB-13229](https://jira.hyperledger.org/browse/FAB-13229) Prepare for next release (2.0.0 on master)
* [4c6cd6c](https://github.com/hyperledger/fabric-chaincode-java/commit/4c6cd6c) [FAB-13236](https://jira.hyperledger.org/browse/FAB-13236) Publish jars to sonatype

## v1.4.0-rc1
Sun Apr  7 15:37:00 IDT 2019

* [2d26e5d](https://github.com/hyperledger/fabric-chaincode-java/commit/2d26e5d) [FAB-13117](https://jira.hyperledger.org/browse/FAB-13117) Release java chaincode 1.4.0-rc1
* [3628832](https://github.com/hyperledger/fabric-chaincode-java/commit/3628832) [FAB-13194](https://jira.hyperledger.org/browse/FAB-13194) Script to publish javaenv multiarch image
* [455100c](https://github.com/hyperledger/fabric-chaincode-java/commit/455100c) [FAB-12426](https://jira.hyperledger.org/browse/FAB-12426) Fix java chaincode return status.
* [ea26118](https://github.com/hyperledger/fabric-chaincode-java/commit/ea26118) [FAB-12197](https://jira.hyperledger.org/browse/FAB-12197) Integration tests
* [3b61085](https://github.com/hyperledger/fabric-chaincode-java/commit/3b61085) [FAB-12110](https://jira.hyperledger.org/browse/FAB-12110) SimpleAsset java chaincode example
* [b46fc70](https://github.com/hyperledger/fabric-chaincode-java/commit/b46fc70) [FAB-12328](https://jira.hyperledger.org/browse/FAB-12328) java cc pagination
* [68685dc](https://github.com/hyperledger/fabric-chaincode-java/commit/68685dc) [FAB-12469](https://jira.hyperledger.org/browse/FAB-12469) Java sbe tests
* [748ea9f](https://github.com/hyperledger/fabric-chaincode-java/commit/748ea9f) [FAB-12468](https://jira.hyperledger.org/browse/FAB-12468) Utility classes for java sbe
* [b4b3a87](https://github.com/hyperledger/fabric-chaincode-java/commit/b4b3a87) [FAB-12467](https://jira.hyperledger.org/browse/FAB-12467) State-based endorsement
* [c32c4c4](https://github.com/hyperledger/fabric-chaincode-java/commit/c32c4c4) [FAB-12568](https://jira.hyperledger.org/browse/FAB-12568) Adding identities to JavaCC
* [fb81132](https://github.com/hyperledger/fabric-chaincode-java/commit/fb81132) Fix LFID for gennadyl
* [7522309](https://github.com/hyperledger/fabric-chaincode-java/commit/7522309) Retire dormant maintainers, suggest new nomination
* [321db66](https://github.com/hyperledger/fabric-chaincode-java/commit/321db66) [FAB-12444](https://jira.hyperledger.org/browse/FAB-12444) Update java cc to baseimage 0.4.14
* [aa177d2](https://github.com/hyperledger/fabric-chaincode-java/commit/aa177d2) [FAB-12347](https://jira.hyperledger.org/browse/FAB-12347) Update java cc to baseimage 0.4.13
* [e9c5602](https://github.com/hyperledger/fabric-chaincode-java/commit/e9c5602) [FAB-12223](https://jira.hyperledger.org/browse/FAB-12223) Adding FAQ file
* [af01201](https://github.com/hyperledger/fabric-chaincode-java/commit/af01201) [FAB-12278](https://jira.hyperledger.org/browse/FAB-12278) Fixing java cc build script
* [52cc6bd](https://github.com/hyperledger/fabric-chaincode-java/commit/52cc6bd) [FAB-12157](https://jira.hyperledger.org/browse/FAB-12157) Hadling chaincode in default package
* [8dcd819](https://github.com/hyperledger/fabric-chaincode-java/commit/8dcd819) [FAB-12152](https://jira.hyperledger.org/browse/FAB-12152) Prepare for next release (1.4.0 on master)
* [cc91af5](https://github.com/hyperledger/fabric-chaincode-java/commit/cc91af5) [FAB-12152](https://jira.hyperledger.org/browse/FAB-12152) Prepare for next release (1.3.0 on master)
* [9cbb36c](https://github.com/hyperledger/fabric-chaincode-java/commit/9cbb36c) [FAB-10525](https://jira.hyperledger.org/browse/FAB-10525) Fix the bug getStateByPartialCompositeKey

## v1.3.0-rc1
Tue Sep 25 15:25:05 EDT 2018

* [224bc4d](https://github.com/hyperledger/fabric-chaincode-java/commit/224bc4d) [FAB-12160](https://jira.hyperledger.org/browse/FAB-12160) Release fabric-chaincode-java v1.3.0-rc1
* [333d91b](https://github.com/hyperledger/fabric-chaincode-java/commit/333d91b) [FAB-12129](https://jira.hyperledger.org/browse/FAB-12129) Update baseimage version
* [01dd207](https://github.com/hyperledger/fabric-chaincode-java/commit/01dd207) [FAB-12115](https://jira.hyperledger.org/browse/FAB-12115) Fix groupId in shim jars
* [5d7ed1c](https://github.com/hyperledger/fabric-chaincode-java/commit/5d7ed1c) [FAB-9519](https://jira.hyperledger.org/browse/FAB-9519) Java shim readmy and tutorial
* [44a1367](https://github.com/hyperledger/fabric-chaincode-java/commit/44a1367) [FAB-12017](https://jira.hyperledger.org/browse/FAB-12017) Adding java docs
* [56e2a11](https://github.com/hyperledger/fabric-chaincode-java/commit/56e2a11) [FAB-11839](https://jira.hyperledger.org/browse/FAB-11839) Adding FVT tests
* [ac70a6f](https://github.com/hyperledger/fabric-chaincode-java/commit/ac70a6f) [FAB-10032](https://jira.hyperledger.org/browse/FAB-10032) Adding unit test
* [ac6c906](https://github.com/hyperledger/fabric-chaincode-java/commit/ac6c906) [FAB-11750](https://jira.hyperledger.org/browse/FAB-11750) Updating version to 1.3.0
* [e80ba7c](https://github.com/hyperledger/fabric-chaincode-java/commit/e80ba7c) [FAB-11288](https://jira.hyperledger.org/browse/FAB-11288) Adding private data functions
* [79497bc](https://github.com/hyperledger/fabric-chaincode-java/commit/79497bc) [FAB-11741](https://jira.hyperledger.org/browse/FAB-11741) Reformat code
* [71ca2a2](https://github.com/hyperledger/fabric-chaincode-java/commit/71ca2a2) [FAB-11664](https://jira.hyperledger.org/browse/FAB-11664) Enable separate docker build
* [52f2c8b](https://github.com/hyperledger/fabric-chaincode-java/commit/52f2c8b) [FAB-11485](https://jira.hyperledger.org/browse/FAB-11485) Fixing empty proposal handling
* [2b35a46](https://github.com/hyperledger/fabric-chaincode-java/commit/2b35a46) [FAB-11304](https://jira.hyperledger.org/browse/FAB-11304) Handling READY message from peer
* [4b27549](https://github.com/hyperledger/fabric-chaincode-java/commit/4b27549) [FAB-9380](https://jira.hyperledger.org/browse/FAB-9380) remove FSM from java chaincode shim
* [f5e6fb4](https://github.com/hyperledger/fabric-chaincode-java/commit/f5e6fb4) [FAB-9920](https://jira.hyperledger.org/browse/FAB-9920) Java shim mutual TLS
* [d295c96](https://github.com/hyperledger/fabric-chaincode-java/commit/d295c96) [FAB-9424](https://jira.hyperledger.org/browse/FAB-9424) Adding docker build support
* [1cb0493](https://github.com/hyperledger/fabric-chaincode-java/commit/1cb0493) [FAB-10845](https://jira.hyperledger.org/browse/FAB-10845) Remove protos files
* [6c8abf4](https://github.com/hyperledger/fabric-chaincode-java/commit/6c8abf4) [FAB-9416](https://jira.hyperledger.org/browse/FAB-9416): Add gradle task to update protos
* [e3be99e](https://github.com/hyperledger/fabric-chaincode-java/commit/e3be99e) [FAB-9407](https://jira.hyperledger.org/browse/FAB-9407): Extract JavaCC shim protos
* [e4c9867](https://github.com/hyperledger/fabric-chaincode-java/commit/e4c9867) [FAB-9359](https://jira.hyperledger.org/browse/FAB-9359) add CODE_OF_CONDUCT.md and other docs
* [8772201](https://github.com/hyperledger/fabric-chaincode-java/commit/8772201) [FAB-9063](https://jira.hyperledger.org/browse/FAB-9063) Provide link to Fabric committers
* [4a9406b](https://github.com/hyperledger/fabric-chaincode-java/commit/4a9406b) Add Maintainers to fabric-chaincode-java
* [091a081](https://github.com/hyperledger/fabric-chaincode-java/commit/091a081) [FAB-8986](https://jira.hyperledger.org/browse/FAB-8986): Harden gradle build configuration
* [bb958eb](https://github.com/hyperledger/fabric-chaincode-java/commit/bb958eb) [FAB-8346](https://jira.hyperledger.org/browse/FAB-8346) sync chaincode proto files w/ fabric
* [c399853](https://github.com/hyperledger/fabric-chaincode-java/commit/c399853) [FAB-7989](https://jira.hyperledger.org/browse/FAB-7989) --peerAddress should be --peer.address
* [308eb63](https://github.com/hyperledger/fabric-chaincode-java/commit/308eb63) [FAB-7626](https://jira.hyperledger.org/browse/FAB-7626) Move to Gradle 4.4.1
* [d73edd6](https://github.com/hyperledger/fabric-chaincode-java/commit/d73edd6) [FAB-7294](https://jira.hyperledger.org/browse/FAB-7294) enable shim jar publication
* [d9c463e](https://github.com/hyperledger/fabric-chaincode-java/commit/d9c463e) [FAB-7091](https://jira.hyperledger.org/browse/FAB-7091) Extract StreamObserver out of ChaincodeBase
* [e3644f4](https://github.com/hyperledger/fabric-chaincode-java/commit/e3644f4) [FAB-6889](https://jira.hyperledger.org/browse/FAB-6889) add channel ID to chaincode message
* [2eda08b](https://github.com/hyperledger/fabric-chaincode-java/commit/2eda08b) [FAB-6726](https://jira.hyperledger.org/browse/FAB-6726) move to gradle 4.2.1
* [5cf4f73](https://github.com/hyperledger/fabric-chaincode-java/commit/5cf4f73) [FAB-3650](https://jira.hyperledger.org/browse/FAB-3650) add getBinding() API
* [a3b7d71](https://github.com/hyperledger/fabric-chaincode-java/commit/a3b7d71) [FAB-3649](https://jira.hyperledger.org/browse/FAB-3649) add getTransient() API
* [3cb86b0](https://github.com/hyperledger/fabric-chaincode-java/commit/3cb86b0) [FAB-3648](https://jira.hyperledger.org/browse/FAB-3648) add getCreator() API
* [5982e40](https://github.com/hyperledger/fabric-chaincode-java/commit/5982e40) [FAB-3653](https://jira.hyperledger.org/browse/FAB-3653) add getTxTimestamp() API
* [5566a62](https://github.com/hyperledger/fabric-chaincode-java/commit/5566a62) [FAB-6681](https://jira.hyperledger.org/browse/FAB-6681) Add code coverage report and verification
* [64de43a](https://github.com/hyperledger/fabric-chaincode-java/commit/64de43a) [FAB-6637](https://jira.hyperledger.org/browse/FAB-6637) add license check to fabric-chaincode-java
* [9f2f4f9](https://github.com/hyperledger/fabric-chaincode-java/commit/9f2f4f9) [FAB-3470](https://jira.hyperledger.org/browse/FAB-3470) update composite key format
* [61b8fc2](https://github.com/hyperledger/fabric-chaincode-java/commit/61b8fc2) [FAB-3651](https://jira.hyperledger.org/browse/FAB-3651) add getSignedProposal() API
* [8c2352a](https://github.com/hyperledger/fabric-chaincode-java/commit/8c2352a) [FAB-6494](https://jira.hyperledger.org/browse/FAB-6494) Add unit tests for ChaincodeStubImpl
* [b58bcc2](https://github.com/hyperledger/fabric-chaincode-java/commit/b58bcc2) Update Jim's RC ID in MAINTAINERS.rst
* [02520ac](https://github.com/hyperledger/fabric-chaincode-java/commit/02520ac) Initial MAINTAINERS file
* [915824f](https://github.com/hyperledger/fabric-chaincode-java/commit/915824f) [FAB-6435](https://jira.hyperledger.org/browse/FAB-6435) Copy Java shim from fabric repository
* [067b712](https://github.com/hyperledger/fabric-chaincode-java/commit/067b712) [FAB-5928](https://jira.hyperledger.org/browse/FAB-5928) Initial gradle build script



