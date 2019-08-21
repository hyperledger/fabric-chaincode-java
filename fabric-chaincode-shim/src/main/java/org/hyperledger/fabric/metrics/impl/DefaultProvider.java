/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.metrics.impl;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.metrics.TaskMetricsCollector;

/**
 * Simple default provider that logs to the org.hyperledger.Performance logger the basic metrics
 *
 */
public class DefaultProvider implements MetricsProvider {
    static Logger perflogger = Logger.getLogger(Logging.PERFLOGGER);
    
    private TaskMetricsCollector taskService;

	public DefaultProvider() {
	    perflogger.info("Default Metrics Provider started");
	}

	@Override
	public void setTaskMetricsCollector(TaskMetricsCollector taskService) {
	    this.taskService = taskService;
	}

    @Override
    public void initialize(Properties props) {
        Timer metricTimer = new Timer(true);
        metricTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DefaultProvider.this.logMetrics();
            }
        }, 0, 5000);
        
    }

    protected void logMetrics() {

        perflogger.info(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append(String.format(" \"active_count\":%d ", DefaultProvider.this.taskService.getActiveCount())).append(',');
            sb.append(String.format(" \"pool_size\":%d ", DefaultProvider.this.taskService.getPoolSize())).append(',');
            sb.append(String.format(" \"core_pool_size\":%d ", DefaultProvider.this.taskService.getCorePoolSize())).append(',');
            sb.append(String.format(" \"current_task_count\":%d ", DefaultProvider.this.taskService.getCurrentTaskCount())).append(',');
            sb.append(String.format(" \"current_queue_depth\":%d ", DefaultProvider.this.taskService.getCurrentQueueCount()));
            return sb.append('}').toString(); 
        });
        
    }

}