/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.ledger;

/**
 * QueryResultsIterator allows a chaincode to iterate over a set of key/value pairs returned by range, execute and history queries.
 *
 * @param <T> the type of elements returned by the iterator
 */
public interface QueryResultsIterator<T> extends Iterable<T>, AutoCloseable {
}

