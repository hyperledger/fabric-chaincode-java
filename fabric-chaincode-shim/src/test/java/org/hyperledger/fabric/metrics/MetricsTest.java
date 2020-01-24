/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.hyperledger.fabric.metrics.impl.DefaultProvider;
import org.hyperledger.fabric.metrics.impl.NullProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MetricsTest {

    public static class TestProvider implements MetricsProvider {

        public TestProvider() {

        }

        @Override
        public void setTaskMetricsCollector(final TaskMetricsCollector taskService) {
        }

        @Override
        public void initialize(final Properties props) {
        }

    }

    @Nested
    @DisplayName("Metrics initialize")
    class Initialize {

        @Test
        public void metricsDisabled() {
            final MetricsProvider provider = Metrics.initialize(new Properties());
            assertThat(provider).isExactlyInstanceOf(NullProvider.class);
        }

        @Test
        public void metricsEnabledUnknownProvider() {
            final Properties props = new Properties();
            props.put("CHAINCODE_METRICS_PROVIDER", "org.example.metrics.provider");
            props.put("CHAINCODE_METRICS_ENABLED", "true");

            assertThrows(RuntimeException.class, () -> {
                final MetricsProvider provider = Metrics.initialize(props);
            }, "Unable to start metrics");
        }

        @Test
        public void metricsNoProvider() {
            final Properties props = new Properties();
            props.put("CHAINCODE_METRICS_ENABLED", "true");

            final MetricsProvider provider = Metrics.initialize(props);
            assertTrue(provider instanceof DefaultProvider);

        }

        @Test
        public void metricsValid() {
            final Properties props = new Properties();
            props.put("CHAINCODE_METRICS_PROVIDER", MetricsTest.TestProvider.class.getName());
            props.put("CHAINCODE_METRICS_ENABLED", "true");
            final MetricsProvider provider = Metrics.initialize(props);

            assertThat(provider).isExactlyInstanceOf(MetricsTest.TestProvider.class);
        }

    }

}
