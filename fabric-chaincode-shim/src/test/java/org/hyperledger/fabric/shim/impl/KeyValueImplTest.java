/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.ledger.queryresult.KV;
import org.junit.jupiter.api.Test;

final class KeyValueImplTest {

    @Test
    void testKeyValueImpl() {
        new KeyValueImpl(KV.newBuilder()
                .setKey("key")
                .setValue(ByteString.copyFromUtf8("value"))
                .build());
    }

    @Test
    void testGetKey() {
        final KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
                .setKey("key")
                .setValue(ByteString.copyFromUtf8("value"))
                .build());
        assertThat(kv.getKey()).isEqualTo("key");
    }

    @Test
    void testGetValue() {
        final KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
                .setKey("key")
                .setValue(ByteString.copyFromUtf8("value"))
                .build());
        assertThat(kv.getValue()).isEqualTo("value".getBytes(UTF_8));
    }

    @Test
    void testGetStringValue() {
        final KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
                .setKey("key")
                .setValue(ByteString.copyFromUtf8("value"))
                .build());
        assertThat(kv.getStringValue()).isEqualTo("value");
    }

    @Test
    void testHashCode() {
        final KeyValueImpl kv1 = new KeyValueImpl(KV.newBuilder().build());
        final KeyValueImpl kv2 = new KeyValueImpl(KV.newBuilder().build());

        assertThat(kv1.hashCode()).isEqualTo(kv2.hashCode());
    }

    @Test
    void testEquals() {
        final KeyValueImpl kv1 = new KeyValueImpl(KV.newBuilder()
                .setKey("a")
                .setValue(ByteString.copyFromUtf8("valueA"))
                .build());

        final KeyValueImpl kv2 = new KeyValueImpl(KV.newBuilder()
                .setKey("a")
                .setValue(ByteString.copyFromUtf8("valueB"))
                .build());

        final KeyValueImpl kv3 = new KeyValueImpl(KV.newBuilder()
                .setKey("b")
                .setValue(ByteString.copyFromUtf8("valueA"))
                .build());

        final KeyValueImpl kv4 = new KeyValueImpl(KV.newBuilder()
                .setKey("a")
                .setValue(ByteString.copyFromUtf8("valueA"))
                .build());

        assertThat(kv1).isNotEqualTo(kv2);
        assertThat(kv1).isNotEqualTo(kv3);
        assertThat(kv1).isEqualTo(kv4);
    }
}
