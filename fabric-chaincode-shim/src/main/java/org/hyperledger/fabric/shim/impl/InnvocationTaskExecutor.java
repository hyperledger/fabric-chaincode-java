/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;

public class InnvocationTaskExecutor extends ThreadPoolExecutor {
	private static Logger logger = Logging.getLogger(InnvocationTaskExecutor.class.getName());

	public InnvocationTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory factory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory, handler);
		prestartCoreThread();
		logger.info("Thread pool created");
	}

	AtomicInteger count = new AtomicInteger();

	@Override
	protected void beforeExecute(Thread thread, Runnable task) {
		super.beforeExecute(thread, task);
		count.incrementAndGet();

	}

	@Override
	protected void afterExecute(Runnable task, Throwable throwable) {
		count.decrementAndGet();
		super.afterExecute(task, throwable);
	}

	public int getCurrentTaskCount() {
		return count.get();
	}

	public int getCurrentQueueCount() {
		return this.getQueue().size();
	}

}
