/*
* Copyright 2020 IBM All Rights Reserved.
*
* SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim;

import java.io.IOException;

public final class ChaincodeServerProperties {

    private int portChaincodeServer = 9999;
    private int maxInboundMetadataSize = 100 * 1024 * 1024;
    private int maxInboundMessageSize = 100 * 1024 * 1024;
    private int maxConnectionAgeSeconds = 5;
    private int keepAliveTimeoutSeconds = 20;
    private int permitKeepAliveTimeMinutes = 1;
    private int keepAliveTimeMinutes = 1;
    private boolean permitKeepAliveWithoutCalls = true;
    private String keyPassword;
    private String keyCertChainFile;
    private String keyFile;
    private String trustCertCollectionFile;
    private boolean tlsEnabled = false;

    public ChaincodeServerProperties() {
    }

    public ChaincodeServerProperties(
        int portChaincodeServer, int maxInboundMetadataSize,
        int maxInboundMessageSize, int maxConnectionAgeSeconds,
        int keepAliveTimeoutSeconds, int permitKeepAliveTimeMinutes,
        int keepAliveTimeMinutes, boolean permitKeepAliveWithoutCalls) {

        this.portChaincodeServer = portChaincodeServer;
        this.maxInboundMetadataSize = maxInboundMetadataSize;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.maxConnectionAgeSeconds = maxConnectionAgeSeconds;
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
        this.permitKeepAliveTimeMinutes = permitKeepAliveTimeMinutes;
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    public int getMaxInboundMetadataSize() {
        return maxInboundMetadataSize;
    }

    public void setMaxInboundMetadataSize(int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public int getMaxConnectionAgeSeconds() {
        return maxConnectionAgeSeconds;
    }

    public void setMaxConnectionAgeSeconds(int maxConnectionAgeSeconds) {
        this.maxConnectionAgeSeconds = maxConnectionAgeSeconds;
    }

    public int getKeepAliveTimeoutSeconds() {
        return keepAliveTimeoutSeconds;
    }

    public void setKeepAliveTimeoutSeconds(int keepAliveTimeoutSeconds) {
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
    }

    public int getPermitKeepAliveTimeMinutes() {
        return permitKeepAliveTimeMinutes;
    }

    public void setPermitKeepAliveTimeMinutes(int permitKeepAliveTimeMinutes) {
        this.permitKeepAliveTimeMinutes = permitKeepAliveTimeMinutes;
    }

    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    public void setKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
    }

    public boolean getPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public int getPortChaincodeServer() {
        return portChaincodeServer;
    }

    public void setPortChaincodeServer(int portChaincodeServer) {
        this.portChaincodeServer = portChaincodeServer;
    }

    public boolean isPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public void setPermitKeepAliveWithoutCalls(boolean permitKeepAliveWithoutCalls) {
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyCertChainFile() {
        return keyCertChainFile;
    }

    public void setKeyCertChainFile(String keyCertChainFile) {
        this.keyCertChainFile = keyCertChainFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getTrustCertCollectionFile() {
        return trustCertCollectionFile;
    }

    public void setTrustCertCollectionFile(String trustCertCollectionFile) {
        this.trustCertCollectionFile = trustCertCollectionFile;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public void validate() throws IOException {
        if (this.getPortChaincodeServer() <= 0) {
            throw new IOException("chaincodeServerProperties.getPortChaincodeServer() must be more then 0");
        }
        if (this.getKeepAliveTimeMinutes() <= 0) {
            throw new IOException("chaincodeServerProperties.getKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getKeepAliveTimeoutSeconds() <= 0) {
            throw new IOException("chaincodeServerProperties.getKeepAliveTimeoutSeconds() must be more then 0");
        }
        if (this.getPermitKeepAliveTimeMinutes() <= 0) {
            throw new IOException("chaincodeServerProperties.getPermitKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getMaxConnectionAgeSeconds() <= 0) {
            throw new IOException("chaincodeServerProperties.getMaxConnectionAgeSeconds() must be more then 0");
        }
        if (this.getMaxInboundMetadataSize() <= 0) {
            throw new IOException("chaincodeServerProperties.getMaxInboundMetadataSize() must be more then 0");
        }
        if (this.getMaxInboundMessageSize() <= 0) {
            throw new IOException("chaincodeServerProperties.getMaxInboundMessageSize() must be more then 0");
        }
        if (this.isTlsEnabled() && (this.getKeyCertChainFile() == null || this.getKeyCertChainFile().isEmpty()
            || this.getKeyFile() == null || this.getKeyFile().isEmpty())) {
            throw new IOException("if chaincodeServerProperties.isTlsEnabled() must be more specified"
                + " chaincodeServerProperties.getKeyCertChainFile() and chaincodeServerProperties.getKeyFile()"
                + " with optional chaincodeServerProperties.getKeyPassword()");
        }
    }
}