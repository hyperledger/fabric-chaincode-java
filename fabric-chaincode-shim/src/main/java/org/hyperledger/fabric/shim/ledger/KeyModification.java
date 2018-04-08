/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.ledger;

public interface KeyModification {

	/**
	 * Returns the transaction id.
	 *
	 * @return
	 */
	String getTxId();

	/**
	 * Returns the key's value at the time returned by {@link #getTimestamp()}.
	 *
	 * @return
	 */
	byte[] getValue();

	/**
	 * Returns the key's value at the time returned by {@link #getTimestamp()},
	 * decoded as a UTF-8 string.
	 *
	 * @return
	 */
	String getStringValue();

	/**
	 * Returns the timestamp of the key modification entry.
	 *
	 * @return
	 */
	java.time.Instant getTimestamp();

	/**
	 * Returns the deletion marker.
	 *
	 * @return
	 */
	boolean isDeleted();

}