/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics;

/**
 * Collect metrics relating to the task execution.
 *
 * The task execution (of which each fabric transaction is one) is backed by a
 * thread pool that implements this interface. As that is an implementation
 * class this interface abstracts the information available from it (as far as
 * metrics go).
 *
 */
public interface TaskMetricsCollector {

    /**
     * Currently executing tasks.
     *
     * @return int &gt; 0
     */
    int getCurrentTaskCount();

    /**
     * Currently waiting tasks; should not be a higher number.
     *
     * @return int &gt; 0
     */
    int getCurrentQueueCount();

    /**
     * Currently executing threads.
     *
     * @return int &gt; 0
     */
    int getActiveCount();

    /**
     * Gets the current size of the pool.
     *
     * @return int &gt; 0
     */
    int getPoolSize();

    /**
     * Gets the core (minimum) pool size.
     *
     * @return int &gt; 0
     */
    int getCorePoolSize();

    /**
     * Gets the largest pool size so far.
     *
     * @return int &gt; 0
     */
    int getLargestPoolSize();

    /**
     * Gets the upper limit pool size.
     *
     * @return int &gt; 0
     */
    int getMaximumPoolSize();
}
