/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.ledger.queryresult.KV;
import org.hyperledger.fabric.shim.ledger.KeyValue;

class KeyValueImpl implements KeyValue {

    private final String key;
    private final ByteString value;

    KeyValueImpl(final KV kv) {
        this.key = kv.getKey();
        this.value = kv.getValue();
    }

    @Override
    public String getKey() {
        return key;
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
    public int hashCode() {
        final int prime = 31;
        int result = key.hashCode();
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

        final KeyValueImpl that = (KeyValueImpl) other;
        return this.key.equals(that.key) && this.value.equals(that.value);
    }
}
