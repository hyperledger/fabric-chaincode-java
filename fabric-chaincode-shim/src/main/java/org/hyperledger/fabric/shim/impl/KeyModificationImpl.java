/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import java.time.Instant;
import org.hyperledger.fabric.shim.ledger.KeyModification;

public final class KeyModificationImpl implements KeyModification {

    private final String txId;
    private final ByteString value;
    private final Instant timestamp;
    private final boolean deleted;

    KeyModificationImpl(final org.hyperledger.fabric.protos.ledger.queryresult.KeyModification km) {
        this.txId = km.getTxId();
        this.value = km.getValue();
        this.timestamp = Instant.ofEpochSecond(
                km.getTimestamp().getSeconds(), km.getTimestamp().getNanos());
        this.deleted = km.getIsDelete();
    }

    @Override
    public String getTxId() {
        return txId;
    }

    @Override
    public byte[] getValue() {
        return value.toByteArray();
    }

    @Override
    public String getStringValue() {
        return value.toStringUtf8();
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Boolean.hashCode(deleted);
        result = prime * result + timestamp.hashCode();
        result = prime * result + txId.hashCode();
        result = prime * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        final KeyModificationImpl that = (KeyModificationImpl) other;
        return this.deleted == that.deleted
                && this.timestamp.equals(that.timestamp)
                && this.txId.equals(that.txId)
                && this.value.equals(that.value);
    }
}
