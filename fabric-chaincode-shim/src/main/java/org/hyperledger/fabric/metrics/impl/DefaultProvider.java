/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics.impl;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.metrics.TaskMetricsCollector;

/** Simple default provider that logs to the org.hyperledger.Performance logger the basic metrics. */
public final class DefaultProvider implements MetricsProvider {
    private static final Logger PERFLOGGER = Logger.getLogger(Logging.PERFLOGGER);
    private static final int TIME_INTERVAL = 5000;

    private TaskMetricsCollector taskService;

    /** */
    public DefaultProvider() {
        PERFLOGGER.info("Default Metrics Provider started");
    }

    @Override
    public void setTaskMetricsCollector(final TaskMetricsCollector taskService) {
        this.taskService = taskService;
    }

    @Override
    public void initialize(final Properties props) {
        final Timer metricTimer = new Timer(true);
        metricTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        DefaultProvider.this.logMetrics();
                    }
                },
                0,
                TIME_INTERVAL);
    }

    void logMetrics() {
        PERFLOGGER.info(() -> {
            if (taskService == null) {
                return "No Metrics Provider service yet";
            }
            return '{'
                    + String.format(" \"active_count\":%d ", taskService.getActiveCount())
                    + ','
                    + String.format(" \"pool_size\":%d ", taskService.getPoolSize())
                    + ','
                    + String.format(" \"core_pool_size\":%d ", taskService.getCorePoolSize())
                    + ','
                    + String.format(" \"current_task_count\":%d ", taskService.getCurrentTaskCount())
                    + ','
                    + String.format(" \"current_queue_depth\":%d ", taskService.getCurrentQueueCount())
                    + '}';
        });
    }
}
