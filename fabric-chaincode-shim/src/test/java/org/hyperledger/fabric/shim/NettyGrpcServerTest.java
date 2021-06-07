/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

class NettyGrpcServerTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void setEnv() {
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "false");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");
    }

    @AfterEach
    void clearEnv() {
        environmentVariables.clear("CORE_CHAINCODE_ID_NAME");
        environmentVariables.clear("CORE_PEER_ADDRESS");
        environmentVariables.clear("CORE_PEER_TLS_ENABLED");
        environmentVariables.clear("CORE_PEER_TLS_ROOTCERT_FILE");
        environmentVariables.clear("CORE_TLS_CLIENT_KEY_PATH");
        environmentVariables.clear("CORE_TLS_CLIENT_CERT_PATH");
    }

    @Test
    void initNoTls() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, new ChaincodeServerProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void validationNoChaincodeServerPropertiesg() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, null);
                },
                "ChaincodeServerProperties must be specified"
        );
    }

    @Test
    void validationPortChaincodeServer() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setPortChaincodeServer(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getPortChaincodeServer() must be more then 0"
        );
    }

    @Test
    void validationKeepAliveTimeMinutes() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setKeepAliveTimeMinutes(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getKeepAliveTimeMinutes() must be more then 0"
        );
    }

    @Test
    void validationKeepAliveTimeoutSeconds() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setKeepAliveTimeoutSeconds(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getKeepAliveTimeoutSeconds() must be more then 0"
        );
    }

    @Test
    void validationPermitKeepAliveTimeMinutes() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setPermitKeepAliveTimeMinutes(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getPermitKeepAliveTimeMinutes() must be more then 0"
        );
    }

    @Test
    void validationMaxConnectionAgeSeconds() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setMaxConnectionAgeSeconds(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getMaxConnectionAgeSeconds() must be more then 0"
        );
    }

    @Test
    void validationMaxInboundMetadataSize() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setMaxInboundMetadataSize(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getMaxInboundMetadataSize() must be more then 0"
        );
    }

    @Test
    void validationMaxInboundMessageSize() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setMaxInboundMessageSize(-1);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getMaxInboundMessageSize() must be more then 0"
        );
    }

    @Test
    void validationTlsEnabledButKeyNotSet() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
                    chaincodeServerProperties.setTlsEnabled(true);
                    chaincodeServerProperties.setKeyFile(null);
                    chaincodeServerProperties.setKeyCertChainFile(null);
                    chaincodeServerProperties.setKeyPassword(null);
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
                },
                "ChaincodeServerProperties.getMaxInboundMessageSize() must be more then 0"
        );
    }

    @Test
    void initNull() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(null, new ChaincodeServerProperties());
                },
                "chaincode must be specified"
        );
    }

    @Test
    void initNullEnvNotSet() {
        clearEnv();
        try {
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(null, new ChaincodeServerProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void initEnvNotSet() {
        clearEnv();
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, new ChaincodeServerProperties());
                }
        );
    }

    @Test
    void initEnvSetPortChaincodeServerAndCoreChaincodeIdName() throws IOException {
        clearEnv();
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        ChaincodeBase chaincodeBase = new EmptyChaincode();
        chaincodeBase.processEnvironmentOptions();

        Properties props = chaincodeBase.getChaincodeConfig();
        Metrics.initialize(props);

        ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, new ChaincodeServerProperties());

    }

    @Test
    void startAndStopSetCoreChaincodeIdName() {
        clearEnv();
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        try {
            ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            Properties props = chaincodeBase.getChaincodeConfig();
            Metrics.initialize(props);

            ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, new ChaincodeServerProperties());
            new Thread(() -> {
                try {
                    chaincodeServer.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chaincodeServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStop() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, new ChaincodeServerProperties());
            new Thread(() -> {
                try {
                    chaincodeServer.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chaincodeServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStopTlsPassword() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
            chaincodeServerProperties.setTlsEnabled(true);
            chaincodeServerProperties.setKeyFile("src/test/resources/client.key.password-protected");
            chaincodeServerProperties.setKeyCertChainFile("src/test/resources/client.crt");
            chaincodeServerProperties.setKeyPassword("test");
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
            new Thread(() -> {
                try {
                    chaincodeServer.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chaincodeServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStopTlsWithoutPassword() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
            chaincodeServerProperties.setTlsEnabled(true);
            chaincodeServerProperties.setKeyFile("src/test/resources/client.key");
            chaincodeServerProperties.setKeyCertChainFile("src/test/resources/client.crt");
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
            new Thread(() -> {
                try {
                    chaincodeServer.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chaincodeServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
