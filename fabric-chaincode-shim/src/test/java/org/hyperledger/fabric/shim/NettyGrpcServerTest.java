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
            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void validationNoGrpcServerSetting() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, null);
                },
                "GrpcServerSetting must be specified"
        );
    }

    @Test
    void validationPortChaincodeServer() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setPortChaincodeServer(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getPortChaincodeServer() must be more then 0"
        );
    }

    @Test
    void validationKeepAliveTimeMinutes() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setKeepAliveTimeMinutes(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getKeepAliveTimeMinutes() must be more then 0"
        );
    }

    @Test
    void validationKeepAliveTimeoutSeconds() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setKeepAliveTimeoutSeconds(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getKeepAliveTimeoutSeconds() must be more then 0"
        );
    }

    @Test
    void validationPermitKeepAliveTimeMinutes() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setPermitKeepAliveTimeMinutes(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getPermitKeepAliveTimeMinutes() must be more then 0"
        );
    }

    @Test
    void validationMaxConnectionAgeSeconds() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setMaxConnectionAgeSeconds(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getMaxConnectionAgeSeconds() must be more then 0"
        );
    }

    @Test
    void validationMaxInboundMetadataSize() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setMaxInboundMetadataSize(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getMaxInboundMetadataSize() must be more then 0"
        );
    }

    @Test
    void validationMaxInboundMessageSize() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setMaxInboundMessageSize(-1);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getMaxInboundMessageSize() must be more then 0"
        );
    }

    @Test
    void validationTlsEnabledButKeyNotSet() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    final ChaincodeBase chaincodeBase = new EmptyChaincode();
                    final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
                    grpcServerSetting.setTlsEnabled(true);
                    grpcServerSetting.setKeyFile(null);
                    grpcServerSetting.setKeyCertChainFile(null);
                    grpcServerSetting.setKeyPassword(null);
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getMaxInboundMessageSize() must be more then 0"
        );
    }

    @Test
    void initNull() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(null, new ChaincodeServerProperties());
                },
                "chaincode must be specified"
        );
    }

    @Test
    void initNullEnvNotSet() {
        clearEnv();
        try {
            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(null, new ChaincodeServerProperties());
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
                    ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());
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

        ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());

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

            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());
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
            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());
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
            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, new ChaincodeServerProperties());
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
            final ChaincodeServerProperties grpcServerSetting = new ChaincodeServerProperties();
            ChaincodeServer chaincodeServer = new ChaincodeServerImpl(chaincodeBase, grpcServerSetting);
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
