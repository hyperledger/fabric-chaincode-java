/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.INIT;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.REGISTERED;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.TRANSACTION;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeID;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type;
import org.hyperledger.fabric.shim.ChaincodeBase;

public class InnvocationTaskManager {

	private static Logger logger = Logging.getLogger(InnvocationTaskManager.class);
	private static Logger perflogger = Logging.getLogger("org.hyperledger.Performance");

	/**
	 * Get an instance of the Invocation Task Manager
	 *
	 * @param chaincode   Chaincode Instance
	 * @param chaincodeId ID of the chaincode
	 * @return InvocationTaskManager
	 */
	public static InnvocationTaskManager getManager(ChaincodeBase chaincode, ChaincodeID chaincodeId) {
		return new InnvocationTaskManager(chaincode, chaincodeId);
	}

	// Keeping a map here of the tasks that are currently ongoing, and the key
	//
	// Key = txid + channleid
	// One task = one transaction invocation
	private ConcurrentHashMap<String, ChaincodeInnvocationTask> innvocationTasks = new ConcurrentHashMap<>();

	// Way to send back the events and data that make up the requests
	private Consumer<ChaincodeMessage> outgoingMessage;

	// references to the chaincode, and the chaincode id
	private ChaincodeBase chaincode;
	private ChaincodeID chaincodeId;

	// Thread Pool creation and settings
	private int queueSize;
	private int maximumPoolSize;
	private int corePoolSize;
	private long keepAliveTime;
	private TimeUnit unit = TimeUnit.MILLISECONDS;
	private BlockingQueue<Runnable> workQueue;
	ThreadFactory threadFactory = Executors.defaultThreadFactory();
	RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

	InnvocationTaskExecutor taskService;

	/**
	 * New InvocationTaskManager
	 *
	 * @param chaincode   Chaincode Instance
	 * @param chaincodeId ID of the chaincode
	 */
	public InnvocationTaskManager(ChaincodeBase chaincode, ChaincodeID chaincodeId) {
		this.chaincode = chaincode;
		this.chaincodeId = chaincodeId;

		// setup the thread pool here
		Properties props = chaincode.getProperties();
		queueSize = Integer.parseInt((String) props.getOrDefault("TP_QUEUE_SIZE", "1"));
		maximumPoolSize = Integer.parseInt((String) props.getOrDefault("TP_MAX_POOL_SIZE", "1"));
		corePoolSize = Integer.parseInt((String) props.getOrDefault("TP_CORE_POOL_SIZE", "1"));
		keepAliveTime = Long.parseLong((String) props.getOrDefault("TP_KEEP_ALIVE_MS", "5000"));

		workQueue = new LinkedBlockingQueue<Runnable>(queueSize);

		logger.info(() -> "Max Pool Size" + maximumPoolSize);

		taskService = new InnvocationTaskExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);

		Metrics.getProvider().setInnvocationExecutor(taskService);
	}

	/**
	 * Called when a new message has arrived that needs to be marshaled
	 *
	 * @param chaincodeMessage ChaincodeMessage
	 */
	public void onChaincodeMessage(ChaincodeMessage chaincodeMessage) {
		logger.fine(
				() -> String.format("[%-8.8s] %s", chaincodeMessage.getTxid(), ChaincodeBase.toJsonString(chaincodeMessage)));

		// TODO: deal with the message types are not suitable if not in the ready state
		switch (chaincode.getState()) {
		case CREATED:
			handleCreated(chaincodeMessage);
			break;
		case ESTABLISHED:
			handleEstablished(chaincodeMessage);
			break;
		case READY:
			handleReady(chaincodeMessage);
			break;
		default:
			logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", chaincodeMessage.getTxid(),
					chaincodeMessage.getType()));
			break;
		}
	}

	private void handleCreated(ChaincodeMessage message) {
		if (message.getType() == REGISTERED) {
			chaincode.setState(org.hyperledger.fabric.shim.ChaincodeBase.CCState.ESTABLISHED);
			logger.fine(() -> String.format("[%-8.8s] Received REGISTERED: moving to established state", message.getTxid()));
		} else {
			logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
		}
	}

	private void handleEstablished(ChaincodeMessage message) {
		if (message.getType() == READY) {
			chaincode.setState(org.hyperledger.fabric.shim.ChaincodeBase.CCState.READY);
			logger.fine(() -> String.format("[%-8.8s] Received READY: ready for invocations", message.getTxid()));
		} else {
			logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
		}
	}

	private void handleReady(ChaincodeMessage message) {
		switch (message.getType()) {
		case RESPONSE:
			logger.fine(() -> String.format("[%-8.8s] Received RESPONSE: publishing to channel", message.getTxid()));
			sendToTask(message);
			break;
		case ERROR:
			logger.fine(() -> String.format("[%-8.8s] Received ERROR: publishing to channel", message.getTxid()));
			sendToTask(message);
			break;
		case INIT:
			logger.fine(() -> String.format("[%-8.8s] Received INIT: invoking chaincode init", message.getTxid()));
			newTask(message, INIT);
			break;
		case TRANSACTION:
			logger.fine(() -> String.format("[%-8.8s] Received TRANSACTION: invoking chaincode", message.getTxid()));
			newTask(message, TRANSACTION);
			break;
		default:
			logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", message.getTxid(), message.getType()));
			break;
		}
	}

	/**
	 * Send a message from the peer to the correct task. This will be a response to
	 * something like a getState() call.
	 *
	 * @param message ChaincodeMessage from the peer
	 */
	private void sendToTask(ChaincodeMessage message) {
		try {
			perflogger.fine(() -> "> sendToTask " + message.getTxid());

			String key = message.getChannelId() + message.getTxid();
			ChaincodeInnvocationTask task = this.innvocationTasks.get(key);
			task.postMessage(message);

			perflogger.fine(() -> "< sendToTask " + message.getTxid());
		} catch (InterruptedException e) {
			logger.severe(() -> "Failed to send response to the task task " + message.getTxid() + Logging.formatError(e));

			ChaincodeMessage m = ChaincodeMessageFactory.newErrorEventMessage(message.getChannelId(), message.getTxid(),
					"Failed to send response to task");
			this.outgoingMessage.accept(m);
		}
	}

	/**
	 * Create a new task to handle this transaction function.
	 *
	 * @param message ChaincodeMessage to process
	 * @param type    Type of message = INIT or INVOKE. INIT is deprecated in future
	 *                versions
	 * @throws InterruptedException
	 */
	private void newTask(ChaincodeMessage message, Type type) {
		ChaincodeInnvocationTask task = new ChaincodeInnvocationTask(message, type, this.outgoingMessage,
				this.chaincode);

		perflogger.fine(() -> "> newTask:created " + message.getTxid());

		this.innvocationTasks.put(task.getTxKey(), task);

		try {
			perflogger.fine(() -> "> newTask:submitting " + message.getTxid());

			CompletableFuture<ChaincodeMessage> response = new CompletableFuture<ChaincodeMessage>();
			taskService.submit(() -> {
				try {
					response.complete(task.call());
				} catch (CancellationException e) {
					response.cancel(true);
				} catch (Exception e) {
					response.completeExceptionally(e);
				}
			});

			// we have a future of the chaincode message that should be returned.
			// but waiting for this does not need to block this thread
			// setup the response to be sent back to the peer, and move on.
			response.thenRun(() -> {
				innvocationTasks.remove(task.getTxKey());
				perflogger.fine(() -> "< newTask:completed " + message.getTxid());
			});

			perflogger.fine(() -> "< newTask:submitted " + message.getTxid());

		} catch (RejectedExecutionException e) {
			logger.warning(() -> "Failed to submit task " + message.getTxid() + Logging.formatError(e));
			// this means that there is no way that this can be handed off to another
			// thread for processing, and there's no space left in the queue to hold
			// it pending

			ChaincodeMessage m = ChaincodeMessageFactory.newErrorEventMessage(message.getChannelId(), message.getTxid(),
					"Failed to submit task for processing");
			this.outgoingMessage.accept(m);
		}

	}

	/**
	 * Set the Consumer function to be used for sending messages back to the peer
	 *
	 * @param outgoingMessage
	 * @return
	 */
	public InnvocationTaskManager setResponseConsumer(Consumer<ChaincodeMessage> outgoingMessage) {
		this.outgoingMessage = outgoingMessage;

		return this;
	}

	/**
	 * Send the initial protocol message for the 'register' phase
	 *
	 * @return
	 */
	public InnvocationTaskManager register() {

		logger.info(() -> "Registering new chaincode " + this.chaincodeId);
		chaincode.setState(ChaincodeBase.CCState.CREATED);
		this.outgoingMessage.accept(ChaincodeMessageFactory.newRegisterChaincodeMessage(this.chaincodeId));

		return this;
	}

	public void shutdown() {
		// Recommended shutdown process from
		// https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
		// Disable new tasks from being submitted
		this.taskService.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!taskService.awaitTermination(60, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				taskService.shutdownNow();
				// Wait a while for tasks to respond to being cancelled
				if (!taskService.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ex) {
			// (Re-)Cancel if current thread also interrupted
			taskService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
