# Contributing to fabric-chaincode-java

We welcome contributions to the [Hyperledger Fabric](https://hyperledger-fabric.readthedocs.io) Project. There's always plenty to do!

If you have any questions about the project or how to contribute, you can find us in the [fabric-contracts-api](https://discordapp.com/channels/905194001349627914/943090527920877598) channel on Discord.

Here are a few guidelines to help you contribute successfully...

## Issues

All issues are tracked in the issues tab in github. If you find a bug which we don't already know about, you can help us by creating a new issue describing the problem. Please include as much detail as possible to help us track down the cause.If you want to begin contributing code, looking through our open issues is a good way to start. Try looking for recent issues with detailed descriptions first, or ask us on Discord if you're unsure which issue to choose.

## Enhancements

Make sure you have the support of the Hyperledger Fabric community before investing a lot of effort in project enhancements. Please look up the Fabric RFC process for large changes.

## Pull Requests

We use our own forks and [Github Flow](https://guides.github.com/introduction/flow/index.html) to deliver changes to the code. Follow these steps to deliver your first pull request:

1. [Fork the repository](https://guides.github.com/activities/forking/#fork) and create a new branch from `main`.
2. If you've added code that should be tested, add tests!
3. If you've added any new features or made breaking changes, update the documentation.
4. Ensure all the tests pass.
5. Include the JIRA issue number, a descriptive message, and the [Developer Certificate of Origin (DCO) sign-off](https://github.com/probot/dco#how-it-works) on all commit messages.
6. [Issue a pull request](https://guides.github.com/activities/forking/#making-a-pull-request)!
7. [Azure DevOps](https://dev.azure.com/Hyperledger/Fabric-Chaincode-Java) builds must succeed before the pull request can be reviewed and merged.

## Coding Style

Please to try to be consistent with the rest of the code and conform to checkstyle rules where they are provided.

## Code of Conduct Guidelines <a name="conduct"></a>

See our [Code of Conduct Guidelines](../blob/main/CODE_OF_CONDUCT.md).

## Maintainers <a name="maintainers"></a>

Should you have any questions or concerns, please reach out to one of the project's [Maintainers](../blob/main/MAINTAINERS.md).


## How to work with the Codebase

Some useful gradle commands to help with building.  You can add or remove the `--no-daemon` and `-x dependencyCheckAnalyze` as you wish; depending on the performance of you local machine.

```
# build everything , but skip the (slow) dependency checks
./gradlew --no-daemon build -x dependencyCheckAnalyze

# clean up to force tests and compile to rerun
./gradlew clean cleanTest
./gradlew --no-daemon :fabric-chaincode-shim:build -x dependencyCheckAnalyze

# build docker image
./gradlew :fabric-chaincode-docker:buildImage
```

## Hyperledger Fabric

See the
[Hyperledger Fabric contributors guide](http://hyperledger-fabric.readthedocs.io/en/latest/CONTRIBUTING.html) for more details, including other Hyperledger Fabric projects you may wish to contribute to.

---

[![Creative Commons License](https://i.creativecommons.org/l/by/4.0/88x31.png)](http://creativecommons.org/licenses/by/4.0/)  
This work is licensed under a [Creative Commons Attribution 4.0 International License](http://creativecommons.org/licenses/by/4.0/)
