/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import java.util.stream.Stream;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.junit.jupiter.api.Test;

final class KeyModificationImplTest {

    @Test
    void testKeyModificationImpl() {
        new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                .setTxId("txid")
                .setValue(ByteString.copyFromUtf8("value"))
                .setTimestamp(Timestamp.newBuilder().setSeconds(1234567890).setNanos(123456789))
                .setIsDelete(true)
                .build());
    }

    @Test
    void testGetTxId() {
        final KeyModification km =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setTxId("txid")
                        .build());
        assertThat(km.getTxId()).isEqualTo("txid");
    }

    @Test
    void testGetValue() {
        final KeyModification km =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setValue(ByteString.copyFromUtf8("value"))
                        .build());
        assertThat(km.getValue()).isEqualTo("value".getBytes(UTF_8));
    }

    @Test
    void testGetStringValue() {
        final KeyModification km =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setValue(ByteString.copyFromUtf8("value"))
                        .build());
        assertThat(km.getStringValue()).isEqualTo("value");
    }

    @Test
    void testGetTimestamp() {
        final KeyModification km =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setTimestamp(
                                Timestamp.newBuilder().setSeconds(1234567890L).setNanos(123456789))
                        .build());
        assertThat(km.getTimestamp().getEpochSecond()).isEqualTo(1234567890L);
        assertThat(km.getTimestamp().getNano()).isEqualTo(123456789);
    }

    @Test
    void testIsDeleted() {
        Stream.of(true, false).forEach(b -> {
            final KeyModification km = new KeyModificationImpl(
                    org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                            .setIsDelete(b)
                            .build());
            assertThat(km.isDeleted()).isEqualTo(b);
        });
    }

    @Test
    void testHashCode() {
        final KeyModification km1 =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setIsDelete(false)
                        .build());
        final KeyModification km2 =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setIsDelete(false)
                        .build());

        assertThat(km1.hashCode()).isEqualTo(km2.hashCode());
    }

    @Test
    void testEquals() {
        final KeyModification km1 =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setIsDelete(false)
                        .build());
        final KeyModification km2 =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setIsDelete(true)
                        .build());

        final KeyModification km3 =
                new KeyModificationImpl(org.hyperledger.fabric.protos.ledger.queryresult.KeyModification.newBuilder()
                        .setIsDelete(false)
                        .build());

        assertThat(km1).isNotEqualTo(km2);
        assertThat(km1).isEqualTo(km3);
    }
}
