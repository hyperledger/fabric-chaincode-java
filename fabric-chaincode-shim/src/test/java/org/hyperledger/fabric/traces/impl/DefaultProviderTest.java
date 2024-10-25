/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import org.hyperledger.fabric.contract.ChaincodeStubNaiveImpl;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Test;

public class DefaultProviderTest {

    @Test
    public void testDefaultProvider() {
        DefaultTracesProvider provider = new DefaultTracesProvider();
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Span span = provider.createSpan(stub);
        assertThat(span).isNull();
    }
}
