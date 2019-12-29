/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Give possibility to stop runnable execution after specific time, if not ended
 */
public final class TimeoutUtil {

    private TimeoutUtil() {

    }

    public static void runWithTimeout(final Runnable callable, final long timeout, final TimeUnit timeUnit) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread t = new Thread(() -> {
            try {
                callable.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            executor.execute(t);
            if (!latch.await(timeout, timeUnit)) {
                throw new TimeoutException();
            }
        } finally {
            executor.shutdown();
            t.interrupt();
        }
    }
}
