/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.metrics.TaskMetricsCollector;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class DefaultProviderTest {

    @Test
    public void allMethods() {
        MetricsProvider provider = new DefaultProvider();
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
                // TODO Auto-generated method stub
                return 0;
            }
        });

        Logger perfLogger = LogManager.getLogManager().getLogger("org.hyperledger.Performance");
        Level original = perfLogger.getLevel();
        try {
            perfLogger.setLevel(Level.ALL);

            Handler mockHandler = Mockito.mock(Handler.class);
            ArgumentCaptor<LogRecord> argumentCaptor = ArgumentCaptor.forClass(LogRecord.class);
            perfLogger.addHandler(mockHandler);

            provider.initialize(new Properties());
            ((DefaultProvider) provider).logMetrics();
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Mockito.verify(mockHandler, Mockito.atLeast(1)).publish(argumentCaptor.capture());
            LogRecord lr = argumentCaptor.getValue();
            String msg = lr.getMessage();
            assertThat(msg).contains("{ \"active_count\":0 , \"pool_size\":0 , \"core_pool_size\":0 , \"current_task_count\":0 , \"current_queue_depth\":0 ");
        } finally {
            perfLogger.setLevel(original);
        }
    }

}
