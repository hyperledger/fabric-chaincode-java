/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics.impl;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import org.hyperledger.fabric.metrics.TaskMetricsCollector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenTelemetryMetricsProviderTest {

    private static final class FakeMetricsCollector
            extends MetricsServiceGrpc.MetricsServiceImplBase {
        private final List<ResourceMetrics> receivedMetrics = new ArrayList<>();

        @Override
        public void export(
                final ExportMetricsServiceRequest request,
                final StreamObserver<ExportMetricsServiceResponse> responseObserver) {

            receivedMetrics.addAll(request.getResourceMetricsList());
            responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        List<ResourceMetrics> getReceivedMetrics() {
            return receivedMetrics;
        }
    }

    @Test
    void testMetricsSent() throws Exception {
        OpenTelemetryMetricsProvider provider = new OpenTelemetryMetricsProvider();
        provider.setTaskMetricsCollector(new TaskMetricsCollector() {

            @Override
            public int getPoolSize() {
                return 0;
            }

            @Override
            public int getMaximumPoolSize() {
                return 0;
            }

            @Override
            public int getLargestPoolSize() {
                return 0;
            }

            @Override
            public int getCurrentTaskCount() {
                return 0;
            }

            @Override
            public int getCurrentQueueCount() {
                return 0;
            }

            @Override
            public int getCorePoolSize() {
                return 0;
            }

            @Override
            public int getActiveCount() {
                return 0;
            }
        });
        FakeMetricsCollector fakeMetricsCollector = new FakeMetricsCollector();
        Server server =
                NettyServerBuilder.forPort(4317)
                        .addService(fakeMetricsCollector)
                        .build()
                        .start();
        try {

            provider.initialize(new Properties());
            provider.logMetrics();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            assertThat(fakeMetricsCollector.getReceivedMetrics().get(0).
                    getInstrumentationLibraryMetricsList().get(0).getMetricsCount()).isEqualTo(5);
            List<String> names = fakeMetricsCollector.getReceivedMetrics().get(0).
                    getInstrumentationLibraryMetricsList().get(0).getMetricsList().
                    stream().map(Metric::getName).collect(Collectors.toList());
            assertThat(names).containsExactlyInAnyOrder("pool_size",
                    "current_task_count",
                    "core_pool_size",
                    "active_count",
                    "current_queue_depth");

        } finally {
            server.shutdownNow();
        }
    }
}
