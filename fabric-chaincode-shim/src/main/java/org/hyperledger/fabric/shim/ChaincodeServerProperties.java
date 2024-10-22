/*
 * Copyright 2020 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import java.net.SocketAddress;

public final class ChaincodeServerProperties {
    private SocketAddress serverAddress;
    private int maxInboundMetadataSize = 100 * 1024 * 1024; // checkstyle:ignore-line:MagicNumber
    private int maxInboundMessageSize = 100 * 1024 * 1024; // checkstyle:ignore-line:MagicNumber
    private int maxConnectionAgeSeconds = 5; // checkstyle:ignore-line:MagicNumber
    private int keepAliveTimeoutSeconds = 20; // checkstyle:ignore-line:MagicNumber
    private int permitKeepAliveTimeMinutes = 1; // checkstyle:ignore-line:MagicNumber
    private int keepAliveTimeMinutes = 1; // checkstyle:ignore-line:MagicNumber
    private boolean permitKeepAliveWithoutCalls = true;
    private String keyPassword;
    private String keyCertChainFile;
    private String keyFile;
    private String trustCertCollectionFile;
    private boolean tlsEnabled = false;

    /** Constructor using default configuration. */
    public ChaincodeServerProperties() {}

    /**
     * Constructor.
     *
     * @param portChaincodeServer ignored.
     * @param maxInboundMetadataSize the maximum metadata size allowed to be received by the server.
     * @param maxInboundMessageSize the maximum message size allowed to be received by the server.
     * @param maxConnectionAgeSeconds the maximum connection age in seconds.
     * @param keepAliveTimeoutSeconds timeout for a keep-alive ping request in seconds.
     * @param permitKeepAliveTimeMinutes the most aggressive keep-alive time clients are permitted to configure in
     *     minutes.
     * @param keepAliveTimeMinutes delay before server sends a keep-alive in minutes.
     * @param permitKeepAliveWithoutCalls whether clients are allowed to send keep-alive HTTP/2 PINGs even if there are
     *     no outstanding RPCs on the connection.
     */
    // checkstyle:ignore-next-line:ParameterNumber
    public ChaincodeServerProperties(
            final int portChaincodeServer,
            final int maxInboundMetadataSize,
            final int maxInboundMessageSize,
            final int maxConnectionAgeSeconds,
            final int keepAliveTimeoutSeconds,
            final int permitKeepAliveTimeMinutes,
            final int keepAliveTimeMinutes,
            final boolean permitKeepAliveWithoutCalls) {

        this.serverAddress = null;
        this.maxInboundMetadataSize = maxInboundMetadataSize;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.maxConnectionAgeSeconds = maxConnectionAgeSeconds;
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
        this.permitKeepAliveTimeMinutes = permitKeepAliveTimeMinutes;
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    /**
     * The maximum size of metadata allowed to be received.
     *
     * @return The maximum metadata size allowed.
     */
    public int getMaxInboundMetadataSize() {
        return maxInboundMetadataSize;
    }

    /**
     * Sets the maximum metadata size allowed to be received by the server.
     *
     * @param maxInboundMetadataSize The new maximum size allowed for incoming metadata.
     */
    public void setMaxInboundMetadataSize(final int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
    }

    /**
     * The maximum message size allowed to be received by the server.
     *
     * @return the maximum message size allowed.
     */
    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    /**
     * Sets the maximum message size allowed to be received by the server.
     *
     * @param maxInboundMessageSize The new maximum size allowed for incoming messages.
     */
    public void setMaxInboundMessageSize(final int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    /**
     * The maximum connection age.
     *
     * @return The maximum connection age in seconds.
     */
    public int getMaxConnectionAgeSeconds() {
        return maxConnectionAgeSeconds;
    }

    /**
     * Specify a maximum connection age.
     *
     * @param maxConnectionAgeSeconds The maximum connection age in seconds.
     */
    public void setMaxConnectionAgeSeconds(final int maxConnectionAgeSeconds) {
        this.maxConnectionAgeSeconds = maxConnectionAgeSeconds;
    }

    /**
     * The timeout for a keep-alive ping requests.
     *
     * @return timeout in seconds.
     */
    public int getKeepAliveTimeoutSeconds() {
        return keepAliveTimeoutSeconds;
    }

    /**
     * Set the timeout for keep-alive ping requests.
     *
     * @param keepAliveTimeoutSeconds timeout in seconds.
     */
    public void setKeepAliveTimeoutSeconds(final int keepAliveTimeoutSeconds) {
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
    }

    /**
     * The most aggressive keep-alive time clients are permitted to configure.
     *
     * @return time in minutes.
     */
    public int getPermitKeepAliveTimeMinutes() {
        return permitKeepAliveTimeMinutes;
    }

    /**
     * Specify the most aggressive keep-alive time clients are permitted to configure.
     *
     * @param permitKeepAliveTimeMinutes time in minutes.
     */
    public void setPermitKeepAliveTimeMinutes(final int permitKeepAliveTimeMinutes) {
        this.permitKeepAliveTimeMinutes = permitKeepAliveTimeMinutes;
    }

    /**
     * The delay before the server sends a keep-alive.
     *
     * @return delay in minutes.
     */
    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    /**
     * Set the delay before the server sends a keep-alive.
     *
     * @param keepAliveTimeMinutes delay in minutes.
     */
    public void setKeepAliveTimeMinutes(final int keepAliveTimeMinutes) {
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
    }

    /**
     * Whether clients are allowed to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the
     * connection.
     *
     * @return true if clients are allowed to send keep-alive requests without calls; otherwise false.
     */
    public boolean getPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    /**
     * Get the server socket address.
     *
     * @return a socket address.
     */
    public SocketAddress getServerAddress() {
        return serverAddress;
    }

    /**
     * Set the server socket address.
     *
     * @param address a socket address.
     */
    public void setServerAddress(final SocketAddress address) {
        this.serverAddress = address;
    }

    /**
     * Whether clients are allowed to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the
     * connection.
     *
     * @return true if clients are allowed to send keep-alive requests without calls; otherwise false.
     */
    public boolean isPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    /**
     * Specify whether clients are allowed to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the
     * connection.
     *
     * @param permitKeepAliveWithoutCalls Whether to allow clients to send keep-alive requests without calls.
     */
    public void setPermitKeepAliveWithoutCalls(final boolean permitKeepAliveWithoutCalls) {
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
    }

    /**
     * Password used to access the server key.
     *
     * @return a password.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Set the password used to access the server key.
     *
     * @param keyPassword a password.
     */
    public void setKeyPassword(final String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Server keychain file name.
     *
     * @return a file name.
     */
    public String getKeyCertChainFile() {
        return keyCertChainFile;
    }

    /**
     * Set the server keychain file name.
     *
     * @param keyCertChainFile a file name.
     */
    public void setKeyCertChainFile(final String keyCertChainFile) {
        this.keyCertChainFile = keyCertChainFile;
    }

    /**
     * Server key file name.
     *
     * @return a file name.
     */
    public String getKeyFile() {
        return keyFile;
    }

    /**
     * Set the server key file name.
     *
     * @param keyFile a file name.
     */
    public void setKeyFile(final String keyFile) {
        this.keyFile = keyFile;
    }

    /**
     * Server trust certificate collection file name.
     *
     * @return a file name.
     */
    public String getTrustCertCollectionFile() {
        return trustCertCollectionFile;
    }

    /**
     * Set the server trust certificate collection file name.
     *
     * @param trustCertCollectionFile a file name.
     */
    public void setTrustCertCollectionFile(final String trustCertCollectionFile) {
        this.trustCertCollectionFile = trustCertCollectionFile;
    }

    /**
     * Whether TLS is enabled for the server.
     *
     * @return true if TLS is enabled; otherwise false.
     */
    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    /**
     * Set whether TLS is enabled for the server.
     *
     * @param tlsEnabled true to enable TLS; otherwise false.
     */
    public void setTlsEnabled(final boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    /**
     * Check that all the server property values are valid.
     *
     * @throws IllegalArgumentException if any properties are not valid.
     */
    public void validate() {
        if (this.getServerAddress() == null) {
            throw new IllegalArgumentException("chaincodeServerProperties.getServerAddress() must be set");
        }
        if (this.getKeepAliveTimeMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getKeepAliveTimeoutSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getKeepAliveTimeoutSeconds() must be more then 0");
        }
        if (this.getPermitKeepAliveTimeMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getPermitKeepAliveTimeMinutes() must be more then 0");
        }
        if (this.getMaxConnectionAgeSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getMaxConnectionAgeSeconds() must be more then 0");
        }
        if (this.getMaxInboundMetadataSize() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getMaxInboundMetadataSize() must be more then 0");
        }
        if (this.getMaxInboundMessageSize() <= 0) {
            throw new IllegalArgumentException(
                    "chaincodeServerProperties.getMaxInboundMessageSize() must be more then 0");
        }
        if (this.isTlsEnabled()
                && (this.getKeyCertChainFile() == null
                        || this.getKeyCertChainFile().isEmpty()
                        || this.getKeyFile() == null
                        || this.getKeyFile().isEmpty())) {
            throw new IllegalArgumentException("if chaincodeServerProperties.isTlsEnabled() must be more specified"
                    + " chaincodeServerProperties.getKeyCertChainFile() and chaincodeServerProperties.getKeyFile()"
                    + " with optional chaincodeServerProperties.getKeyPassword()");
        }
    }
}
