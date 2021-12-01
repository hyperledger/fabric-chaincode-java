# Java Chaincode -as-a-service

This example shows how to start the chaincode in it's 'as-a-service' mode.
Note that this has been improved over the original mechanism

## Build Changes

There are no changes required to tbe build file. For example the `build.gradle` can be left like this

```
    manifest {
        attributes 'Main-Class': 'org.hyperledger.fabric.contract.ContractRouter'
    }
```

## Contract Code

No changes are required to the contract code. Note that the previous 'bootstrap' main method is not required.

## Environment Variables

The setting of the `CHAINCODE_SERVER_ADDRESS` environment variable will trigger the code to work in the 'as-a-service' mode.  This should be set to the hostname:port that the server will be exposed on.  Typically this would be

```
CHAINCODE_SERVER_ADDRESS=0.0.0.0:9999
```

*NOTE* if `CHAINCODE_SERVER_ADDRESS` is set, and the chaincode is deployed as a regular chaincode, this will result in a failure. The chaincode will still start in 'as-a-service' mode.

The `CORE_CHAINCODE_ID_NAME` must also be set to match the ID used when deploying the chaincode.

*For TLS* ensure that 
- `CORE_PEERT_TLS_ENABLED` is true
- `CORE_PEER_TLS_ROOTCERT_FILE` is set to the certificate of the root CA
- `CORE_TLS_CLIENT_KEY_FILE` and `CORE_TLS_CLIENT_CERT_FILE` if using mutual TLS (PEM encoded)


## Dockerfile

There is an example dockerfile that shows how the chaincode can be built into a container image.
