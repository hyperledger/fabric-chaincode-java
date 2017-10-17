/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult.KV;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class KeyValueImplTest {

	@Test
	public void testKeyValueImpl() {
		new KeyValueImpl(KV.newBuilder()
				.setKey("key")
				.setValue(ByteString.copyFromUtf8("value"))
				.build());
	}

	@Test
	public void testGetKey() {
		KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
				.setKey("key")
				.setValue(ByteString.copyFromUtf8("value"))
				.build());
		assertThat(kv.getKey(), is(equalTo("key")));
	}

	@Test
	public void testGetValue() {
		KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
				.setKey("key")
				.setValue(ByteString.copyFromUtf8("value"))
				.build());
		assertThat(kv.getValue(), is(equalTo("value".getBytes(UTF_8))));
	}

	@Test
	public void testGetStringValue() {
		KeyValueImpl kv = new KeyValueImpl(KV.newBuilder()
				.setKey("key")
				.setValue(ByteString.copyFromUtf8("value"))
				.build());
		assertThat(kv.getStringValue(), is(equalTo("value")));
	}

}
