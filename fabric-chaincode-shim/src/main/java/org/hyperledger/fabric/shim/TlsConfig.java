/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

public final class TlsConfig {
    private boolean disabled = true;
    private String key;
    private String cert;

    /**
     *
     * @return true if tls config disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     *
     * @param disabled true if tls config disabled
     */
    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    /**
     *
     * @return string private key
     */
    public String getKey() {
        return key;
    }

    /**
     *
     * @param key a PKCS#8 private key file in PEM format
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     *
     * @return string certificate
     */
    public String getCert() {
        return cert;
    }

    /**
     *
     * @param cert an X.509 certificate chain file in PEM format
     */
    public void setCert(final String cert) {
        this.cert = cert;
    }

}
