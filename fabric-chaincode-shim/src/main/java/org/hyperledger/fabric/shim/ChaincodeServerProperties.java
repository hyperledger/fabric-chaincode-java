/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import java.net.SocketAddress;

public final class ChaincodeServerProperties {

    private SocketAddress serverAddress;
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
            final int portChaincodeServer, final int maxInboundMetadataSize, final int maxInboundMessageSize,
            final int maxConnectionAgeSeconds, final int keepAliveTimeoutSeconds, final int permitKeepAliveTimeMinutes,
            final int keepAliveTimeMinutes, final boolean permitKeepAliveWithoutCalls) {

        this.serverAddress = null;
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

    public void setMaxInboundMetadataSize(final int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(final int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public int getMaxConnectionAgeSeconds() {
        return maxConnectionAgeSeconds;
    }

    public void setMaxConnectionAgeSeconds(final int maxConnectionAgeSeconds) {
        this.maxConnectionAgeSeconds = maxConnectionAgeSeconds;
    }

    public int getKeepAliveTimeoutSeconds() {
        return keepAliveTimeoutSeconds;
    }

    public void setKeepAliveTimeoutSeconds(final int keepAliveTimeoutSeconds) {
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
    }

    public int getPermitKeepAliveTimeMinutes() {
        return permitKeepAliveTimeMinutes;
    }

    public void setPermitKeepAliveTimeMinutes(final int permitKeepAliveTimeMinutes) {
        this.permitKeepAliveTimeMinutes = permitKeepAliveTimeMinutes;
    }

    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    public void setKeepAliveTimeMinutes(final int keepAliveTimeMinutes) {
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
    }

    public boolean getPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public SocketAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(final SocketAddress address) {
        this.serverAddress = address;
    }

    public boolean isPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public void setPermitKeepAliveWithoutCalls(final boolean permitKeepAliveWithoutCalls) {
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(final String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyCertChainFile() {
        return keyCertChainFile;
    }

    public void setKeyCertChainFile(final String keyCertChainFile) {
        this.keyCertChainFile = keyCertChainFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(final String keyFile) {
        this.keyFile = keyFile;
    }

    public String getTrustCertCollectionFile() {
        return trustCertCollectionFile;
    }

    public void setTrustCertCollectionFile(final String trustCertCollectionFile) {
        this.trustCertCollectionFile = trustCertCollectionFile;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(final boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public void validate() {
        if (this.getServerAddress() == null) {
            throw new IllegalArgumentException("chaincodeServerProperties.getServerAddress() must be set");
        }
        if (this.getKeepAliveTimeMinutes() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getKeepAliveTimeoutSeconds() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getKeepAliveTimeoutSeconds() must be more then 0");
        }
        if (this.getPermitKeepAliveTimeMinutes() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getPermitKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getMaxConnectionAgeSeconds() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getMaxConnectionAgeSeconds() must be more then 0");
        }
        if (this.getMaxInboundMetadataSize() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getMaxInboundMetadataSize() must be more then 0");
        }
        if (this.getMaxInboundMessageSize() <= 0) {
            throw new IllegalArgumentException("chaincodeServerProperties.getMaxInboundMessageSize() must be more then 0");
        }
        if (this.isTlsEnabled() && (this.getKeyCertChainFile() == null || this.getKeyCertChainFile().isEmpty()
            || this.getKeyFile() == null || this.getKeyFile().isEmpty())) {
            throw new IllegalArgumentException("if chaincodeServerProperties.isTlsEnabled() must be more specified"
                + " chaincodeServerProperties.getKeyCertChainFile() and chaincodeServerProperties.getKeyFile()"
                + " with optional chaincodeServerProperties.getKeyPassword()");
        }
    }


}
