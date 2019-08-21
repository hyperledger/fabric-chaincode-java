/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.metrics;

import java.util.function.Supplier;

import org.hyperledger.fabric.shim.impl.InnvocationTaskExecutor;



public interface MetricsProvider {

	void setIdentifier(String id);

	/** add a general purpose number supplier */
	void addGaugeProducer(String purpose, Supplier<Integer> supplier);

	/**
	 * Pass a reference to this service for information gathering
	 *
	 * @param taskService
	 */
	void setInnvocationExecutor(InnvocationTaskExecutor taskService);

}
