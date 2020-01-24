/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * <p>
 * Supports collection of metrics
 * <p>
 * The main metrics that are available are the statistics around the number of
 * tasks that are running, and how the thread pool is handling these.
 *
 * Note a 'task' is a message from the Peer to the Chaincode - this message is
 * either a new transaction, or a response from a stub API, eg getState(). Query
 * apis may return more than one response.
 *
 * To enable metrics ensure that there is a standard format Java properties file
 * called `config.props` in the root of your contract code. For example this
 * path
 *
 * <pre>
 * myjava - contract - project / java / src / main / resources / config.props
 * </pre>
 *
 * This should contain the following
 *
 * <pre>
 * CHAINCODE_METRICS_ENABLED=true
 * TP_CORE_POOL_SIZE=5
 * TP_MAX_POOL_SIZE=5
 * TP_QUEUE_SIZE=5000
 * </pre>
 *
 * The metrics enabled flag will turn on default metrics logging. (it's off by
 * default) The TP values establish the core thread pool size, max thread
 * poolsize, and the number of of tasks that will wait. (5, 5, 5000 are the
 * default values, so don't need to be explicitly specified).
 *
 * If no file is supplied metrics are not enabled, the values shown for the
 * thread pool are used.
 */
package org.hyperledger.fabric.metrics;
