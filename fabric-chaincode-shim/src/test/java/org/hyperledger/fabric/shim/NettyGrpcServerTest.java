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
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());
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
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, null);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setPortChaincodeServer(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setKeepAliveTimeMinutes(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setKeepAliveTimeoutSeconds(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setPermitKeepAliveTimeMinutes(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setMaxConnectionAgeSeconds(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setMaxInboundMetadataSize(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setMaxInboundMessageSize(-1);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
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
                    final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
                    grpcServerSetting.setTlsEnabled(true);
                    grpcServerSetting.setKeyFile(null);
                    grpcServerSetting.setKeyCertChainFile(null);
                    grpcServerSetting.setKeyPassword(null);
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
                },
                "GrpcServerSetting.getMaxInboundMessageSize() must be more then 0"
        );
    }

    @Test
    void initNull() {
        Assertions.assertThrows(
                IOException.class,
                () -> {
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(null, new GrpcServerSetting());
                },
                "chaincode must be specified"
        );
    }

    @Test
    void initNullEnvNotSet() {
        clearEnv();
        try {
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(null, new GrpcServerSetting());
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
                    NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());
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

        NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());

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

            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStop() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStopTlsPassword() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, new GrpcServerSetting());
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startAndStopTlsWithoutPassword() {
        try {
            final ChaincodeBase chaincodeBase = new EmptyChaincode();
            chaincodeBase.processEnvironmentOptions();
            final GrpcServerSetting grpcServerSetting = new GrpcServerSetting();
            NettyGrpcServer nettyGrpcServer = new NettyGrpcServer(chaincodeBase, grpcServerSetting);
            new Thread(() -> {
                try {
                    nettyGrpcServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nettyGrpcServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
