/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces;


import io.opentelemetry.api.trace.Span;
import org.hyperledger.fabric.traces.impl.DefaultTracesProvider;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.traces.impl.NullProvider;
import org.hyperledger.fabric.traces.impl.OpenTelemetryTracesProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TracesTest {

    public static final class TestProvider implements TracesProvider {

        public TestProvider() {

        }

        @Override
        public void initialize(final Properties props) {
        }

        @Override
        public Span createSpan(final ChaincodeStub stub) {
            return null;
        }
    }

    @Nested
    @DisplayName("Traces initialize")
    class Initialize {

        @Test
        public void tracesDisabled() {
            final TracesProvider provider = Traces.initialize(new Properties());
            assertThat(provider).isExactlyInstanceOf(NullProvider.class);
        }

        @Test
        public void tracesEnabledUnknownProvider() {
            final Properties props = new Properties();
            props.put("CHAINCODE_TRACES_PROVIDER", "org.example.traces.provider");
            props.put("CHAINCODE_TRACES_ENABLED", "true");

            assertThrows(RuntimeException.class, () -> {
                final TracesProvider provider = Traces.initialize(props);
            }, "Unable to start traces");
        }

        @Test
        public void tracesNoProvider() {
            final Properties props = new Properties();
            props.put("CHAINCODE_TRACES_ENABLED", "true");

            final TracesProvider provider = Traces.initialize(props);
            assertTrue(provider instanceof DefaultTracesProvider);

        }

        @Test
        public void tracesOpenTelemetryProvider() {
            final Properties props = new Properties();
            props.put("CHAINCODE_TRACES_PROVIDER", "org.hyperledger.fabric.traces.impl.OpenTelemetryTracesProvider");
            props.put("CHAINCODE_TRACES_ENABLED", "true");

            final TracesProvider provider = Traces.initialize(props);
            assertTrue(provider instanceof OpenTelemetryTracesProvider);

        }

        @Test
        public void tracesValid() {
            final Properties props = new Properties();
            props.put("CHAINCODE_TRACES_PROVIDER", TracesTest.TestProvider.class.getName());
            props.put("CHAINCODE_TRACES_ENABLED", "true");
            final TracesProvider provider = Traces.initialize(props);

            assertThat(provider).isExactlyInstanceOf(TracesTest.TestProvider.class);
        }

    }
}
