/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult.KV;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import com.google.protobuf.ByteString;

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
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KeyValueImpl other = (KeyValueImpl) obj;
        if (!key.equals(other.key)) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
