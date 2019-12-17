/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.ledger;

/**
 * QueryResult for history query. Holds a transaction ID, value, timestamp, and
 * delete marker which resulted from a history query.
 */
public interface KeyModification {

    /**
     * Returns the transaction id.
     *
     * @return tx id of modification
     */
    String getTxId();

    /**
     * Returns the key's value at the time returned by {@link #getTimestamp()}.
     *
     * @return value
     */
    byte[] getValue();

    /**
     * Returns the key's value at the time returned by {@link #getTimestamp()},
     * decoded as a UTF-8 string.
     *
     * @return value as string
     */
    String getStringValue();

    /**
     * Returns the timestamp of the key modification entry.
     *
     * @return timestamp
     */
    java.time.Instant getTimestamp();

    /**
     * Returns the deletion marker.
     *
     * @return is key was deleted
     */
    boolean isDeleted();

}
