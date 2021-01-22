/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Provides interfaces and classes to support collection of metrics.
 *
 * <p>
 * The main metrics that are available are the statistics around the number of
 * tasks that are running, and how the thread pool is handling these.
 *
 * <p>
 * Note a 'task' is a message from the Peer to the Chaincode - this message is
 * either a new transaction, or a response from a stub API, eg getState(). Query
 * apis may return more than one response.
 *
 * <p>
 * To enable metrics, add a <code>CHAINCODE_METRICS_ENABLED=true</code> setting
 * to the <code>config.props</code> chaincode configuration file.
 * See the <a href="../../../../index.html">Overview</a> for details of how to
 * configure chaincode.
 */
package org.hyperledger.fabric.metrics;
