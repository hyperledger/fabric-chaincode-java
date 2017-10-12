/*
Copyright IBM 2017 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.hyperledger.fabric.shim.impl;

import java.time.Instant;

import org.hyperledger.fabric.protos.ledger.queryresult.KvQueryResult;
import org.hyperledger.fabric.shim.ledger.KeyModification;

import com.google.protobuf.ByteString;

public class KeyModificationImpl implements KeyModification {

	private final String txId;
	private final ByteString value;
	private final java.time.Instant timestamp;
	private final boolean deleted;

	KeyModificationImpl(KvQueryResult.KeyModification km) {
		this.txId = km.getTxId();
		this.value = km.getValue();
		this.timestamp = Instant.ofEpochSecond(km.getTimestamp().getSeconds(), km.getTimestamp().getNanos());
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
	public java.time.Instant getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((txId == null) ? 0 : txId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		KeyModificationImpl other = (KeyModificationImpl) obj;
		if (deleted != other.deleted) return false;
		if (timestamp == null) {
			if (other.timestamp != null) return false;
		} else if (!timestamp.equals(other.timestamp)) return false;
		if (txId == null) {
			if (other.txId != null) return false;
		} else if (!txId.equals(other.txId)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

}
