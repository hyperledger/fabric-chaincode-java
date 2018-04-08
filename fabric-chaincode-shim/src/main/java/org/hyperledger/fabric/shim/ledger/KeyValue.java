/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.ledger;

/**
 * Query Result associating a state key with a value.
 *
 */
public interface KeyValue {

	/**
	 * Returns the state key.
	 *
	 * @return
	 */
	String getKey();

	/**
	 * Returns the state value.
	 *
	 * @return
	 */
	byte[] getValue();

	/**
	 * Returns the state value, decoded as a UTF-8 string.
	 *
	 * @return
	 */
	String getStringValue();

}