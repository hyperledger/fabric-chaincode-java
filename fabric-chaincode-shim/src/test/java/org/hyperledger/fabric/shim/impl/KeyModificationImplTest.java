/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.junit.Test;

import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class KeyModificationImplTest {

	@Test
	public void testKeyModificationImpl() {
		new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
				.setTxId("txid")
				.setValue(ByteString.copyFromUtf8("value"))
				.setTimestamp(Timestamp.newBuilder()
						.setSeconds(1234567890)
						.setNanos(123456789))
				.setIsDelete(true)
				.build()
				);
	}

	@Test
	public void testGetTxId() {
		final KeyModification km = new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
				.setTxId("txid")
				.build()
				);
		assertThat(km.getTxId(), is(equalTo("txid")));
	}

	@Test
	public void testGetValue() {
		final KeyModification km = new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
				.setValue(ByteString.copyFromUtf8("value"))
				.build()
				);
		assertThat(km.getValue(), is(equalTo("value".getBytes(UTF_8))));
	}

	@Test
	public void testGetStringValue() {
		final KeyModification km = new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
				.setValue(ByteString.copyFromUtf8("value"))
				.build()
				);
		assertThat(km.getStringValue(), is(equalTo("value")));
	}

	@Test
	public void testGetTimestamp() {
		final KeyModification km = new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
				.setTimestamp(Timestamp.newBuilder()
						.setSeconds(1234567890L)
						.setNanos(123456789))
				.build()
				);
		assertThat(km.getTimestamp(), hasProperty("epochSecond", equalTo(1234567890L)));
		assertThat(km.getTimestamp(), hasProperty("nano", equalTo(123456789)));
	}

	@Test
	public void testIsDeleted() {
		Stream.of(true, false)
			.forEach(b -> {
				final KeyModification km = new KeyModificationImpl(KvQueryResult.KeyModification.newBuilder()
						.setIsDelete(b)
						.build()
						);
				assertThat(km.isDeleted(), is(b));
			});
	}

}
