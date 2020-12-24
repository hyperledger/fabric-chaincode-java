/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.api.trace.Span;
import org.hyperledger.fabric.contract.ChaincodeStubNaiveImpl;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenTelemetryTracesProviderTest {

    @Test
    public void testProvider() {
        OpenTelemetryTracesProvider provider = new OpenTelemetryTracesProvider();
        provider.initialize(new Properties());
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Span span = provider.createSpan(stub);
        assertThat(span.isRecording()).isTrue();
    }

    @Test
    public void testPropagator() {
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        Iterator<String> keys = OpenTelemetryTracesProvider.PROPAGATOR.keys(stub).iterator();
        assertThat(keys.next()).isEqualTo("b3");
        assertThat(keys.hasNext()).isFalse();
        assertThat(OpenTelemetryTracesProvider.PROPAGATOR.get(stub, "b3")).isEqualTo("b30");
        assertThat(OpenTelemetryTracesProvider.PROPAGATOR.get(stub, "foo")).isNull();
    }
}
