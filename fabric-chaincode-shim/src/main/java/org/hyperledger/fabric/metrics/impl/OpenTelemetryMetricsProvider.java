/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics.impl;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.metrics.TaskMetricsCollector;

import java.util.Collections;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public final class OpenTelemetryMetricsProvider implements MetricsProvider {

    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    private static final String METRICS_PUSH_INTERVAL = "METRICS_PUSH_INTERVAL";
    private static final int TIME_INTERVAL = 5000;

    private SdkMeterProvider meterSdkProvider;
    private TaskMetricsCollector taskService;

    @Override
    public void initialize(final Properties props) {
        String serviceName = props.getProperty(CORE_CHAINCODE_ID_NAME, "unknown");
        int pushInterval;
        try {
            pushInterval = Integer.parseInt(props.getProperty(METRICS_PUSH_INTERVAL, "5000"));
        } catch (NumberFormatException e) {
            pushInterval = TIME_INTERVAL;
        }
        Resource resource =
                Resource.getDefault()
                        .merge(
                                Resource.create(
                                        Attributes.builder().put(ResourceAttributes.SERVICE_NAME, serviceName).build()));
        this.meterSdkProvider = SdkMeterProvider.builder().setResource(resource).build();

        OtlpGrpcMetricExporter exporter = OtlpGrpcMetricExporter.builder().readProperties(props).readEnvironmentVariables().readSystemProperties().build();

        IntervalMetricReader.builder()
                .setExportIntervalMillis(pushInterval)
                .readProperties(props)
                .readEnvironmentVariables()
                .readSystemProperties()
                .setMetricProducers(
                        Collections.singleton(meterSdkProvider.getMetricProducer()))
                .setMetricExporter(exporter).build();

        final Timer metricTimer = new Timer(true);
        metricTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logMetrics();
            }
        }, 0, TIME_INTERVAL);
    }

    @Override
    public void setTaskMetricsCollector(final TaskMetricsCollector taskService) {
        this.taskService = taskService;
    }

    protected void logMetrics() {

        if (this.taskService == null) {
            return;
        }
        Meter meter = meterSdkProvider.get("chaincode_metrics");

        LongValueRecorder activeCount =
                meter.longValueRecorderBuilder("active_count").build();
        activeCount.record(taskService.getActiveCount());
        LongValueRecorder poolSize =
                meter.longValueRecorderBuilder("pool_size").build();
        poolSize.record(taskService.getPoolSize());
        LongValueRecorder corePoolSize =
                meter.longValueRecorderBuilder("core_pool_size").build();
        corePoolSize.record(taskService.getCorePoolSize());
        LongValueRecorder currentTaskCount =
                meter.longValueRecorderBuilder("current_task_count").build();
        currentTaskCount.record(taskService.getCurrentTaskCount());
        LongValueRecorder currentQueueDepth =
                meter.longValueRecorderBuilder("current_queue_depth").build();
        currentQueueDepth.record(taskService.getCurrentQueueCount());
    }
}
