/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.metrics.impl;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class PrometheusProviderImplTest {

	@Test
	public void createInstance() {
		
		HashMap<String,String> props = new HashMap<String,String>();
		PrometheusProvider pp = new PrometheusProvider(props);
			
	}
	
	@Test
	public void customDestination() {
		HashMap<String,String> props = new HashMap<String,String>();
		props.put("PROMETHEUS_PORT","1414");
		props.put("PROMETHEUS_HOST","wibble");
		PrometheusProvider pp = new PrometheusProvider(props);
		pp.pushMetrics();
	}
	
}
