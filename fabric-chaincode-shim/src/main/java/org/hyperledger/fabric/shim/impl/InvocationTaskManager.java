/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.impl;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.READY;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.REGISTERED;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
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

/**
 * The InvocationTask Manager handles the message level communication with the
 * peer.
 *
 * In the current 1.4 Fabric Protocol this is in practice a singleton - because
 * the peer will ignore multiple 'register' calls. And an instance of this will
 * be created per register call for a given chaincodeID.
 *
 */
public final class InvocationTaskManager {

    private static Logger logger = Logger.getLogger(InvocationTaskManager.class.getName());
    private static Logger perflogger = Logger.getLogger(Logging.PERFLOGGER);

    /**
     * Get an instance of the Invocation Task Manager.
     *
     * @param chaincode   Chaincode Instance
     * @param chaincodeId ID of the chaincode
     * @return InvocationTaskManager
     */
    public static InvocationTaskManager getManager(final ChaincodeBase chaincode, final ChaincodeID chaincodeId) {
        return new InvocationTaskManager(chaincode, chaincodeId);
    }

    // Keeping a map here of the tasks that are currently ongoing, and the key
    //
    // Key = txid + channleid
    // One task = one transaction invocation
    private final ConcurrentHashMap<String, ChaincodeInvocationTask> innvocationTasks = new ConcurrentHashMap<>();

    // Way to send back the events and data that make up the requests
    private Consumer<ChaincodeMessage> outgoingMessage;

    // references to the chaincode, and the chaincode id
    private final ChaincodeBase chaincode;
    private final ChaincodeID chaincodeId;

    // Thread Pool creation and settings
    private final int queueSize;
    private final int maximumPoolSize;
    private final int corePoolSize;
    private final long keepAliveTime;
    private final TimeUnit unit = TimeUnit.MILLISECONDS;
    private final BlockingQueue<Runnable> workQueue;
    private final ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private final RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

    private final InvocationTaskExecutor taskService;

    /**
     * New InvocationTaskManager.
     *
     * @param chaincode   Chaincode Instance
     * @param chaincodeId ID of the chaincode
     */
    public InvocationTaskManager(final ChaincodeBase chaincode, final ChaincodeID chaincodeId) {
        this.chaincode = chaincode;
        this.chaincodeId = chaincodeId;

        // setup the thread pool here
        final Properties props = chaincode.getChaincodeConfig();
        queueSize = Integer.parseInt((String) props.getOrDefault("TP_QUEUE_SIZE", "5000"));
        maximumPoolSize = Integer.parseInt((String) props.getOrDefault("TP_MAX_POOL_SIZE", "5"));
        corePoolSize = Integer.parseInt((String) props.getOrDefault("TP_CORE_POOL_SIZE", "5"));
        keepAliveTime = Long.parseLong((String) props.getOrDefault("TP_KEEP_ALIVE_MS", "5000"));

        logger.info(() -> "Max Pool Size [TP_MAX_POOL_SIZE]" + maximumPoolSize);
        logger.info(() -> "Queue Size [TP_CORE_POOL_SIZE]" + queueSize);
        logger.info(() -> "Core Pool Size [TP_QUEUE_SIZE]" + corePoolSize);
        logger.info(() -> "Keep Alive Time [TP_KEEP_ALIVE_MS]" + keepAliveTime);

        workQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        taskService = new InvocationTaskExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        Metrics.getProvider().setTaskMetricsCollector(taskService);

    }

    /**
     * Called when a new message has arrived that needs to be processed.
     *
     * @param chaincodeMessage ChaincodeMessage
     */
    public void onChaincodeMessage(final ChaincodeMessage chaincodeMessage) {
        logger.fine(() -> String.format("[%-8.8s] %s", chaincodeMessage.getTxid(), ChaincodeBase.toJsonString(chaincodeMessage)));

        try {
            final Type msgType = chaincodeMessage.getType();
            switch (chaincode.getState()) {
            case CREATED:
                if (msgType == REGISTERED) {
                    chaincode.setState(org.hyperledger.fabric.shim.ChaincodeBase.CCState.ESTABLISHED);
                    logger.fine(() -> String.format("[%-8.8s] Received REGISTERED: moving to established state", chaincodeMessage.getTxid()));
                } else {
                    logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", chaincodeMessage.getTxid(), msgType));
                }
                break;
            case ESTABLISHED:
                if (msgType == READY) {
                    chaincode.setState(org.hyperledger.fabric.shim.ChaincodeBase.CCState.READY);
                    logger.fine(() -> String.format("[%-8.8s] Received READY: ready for invocations", chaincodeMessage.getTxid()));
                } else {
                    logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", chaincodeMessage.getTxid(), msgType));
                }
                break;
            case READY:
                handleMsg(chaincodeMessage, msgType);
                break;
            default:
                logger.warning(() -> String.format("[%-8.8s] Received %s: cannot handle", chaincodeMessage.getTxid(), chaincodeMessage.getType()));
                break;
            }
        } catch (final RuntimeException e) {
            // catch any issues with say the comms dropping or something else completely
            // unknown
            // and shutdown the pool
            this.shutdown();
            throw e;
        }
    }

    /**
     * Key method to take the message, determine if it is a new transaction or an
     * answer (good or bad) to a stub api.
     *
     * @param message
     * @param msgType
     */
    private void handleMsg(final ChaincodeMessage message, final Type msgType) {
        logger.fine(() -> String.format("[%-8.8s] Received %s", message.getTxid(), msgType.toString()));
        switch (msgType) {
        case RESPONSE:
        case ERROR:
            sendToTask(message);
            break;
        case INIT:
        case TRANSACTION:
            newTask(message, msgType);
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
    private void sendToTask(final ChaincodeMessage message) {
        try {
            perflogger.fine(() -> "> sendToTask " + message.getTxid());

            final String key = message.getChannelId() + message.getTxid();
            final ChaincodeInvocationTask task = this.innvocationTasks.get(key);
            if (task == null) {
                throw new InterruptedException("Task hasmap missing entry");
            }
            task.postMessage(message);

            perflogger.fine(() -> "< sendToTask " + message.getTxid());
        } catch (final InterruptedException e) {
            logger.severe(() -> "Failed to send response to the task task " + message.getTxid() + Logging.formatError(e));

            final ChaincodeMessage m = ChaincodeMessageFactory.newErrorEventMessage(message.getChannelId(), message.getTxid(),
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
    private void newTask(final ChaincodeMessage message, final Type type) {
        final ChaincodeInvocationTask task = new ChaincodeInvocationTask(message, type, this.outgoingMessage, this.chaincode);

        perflogger.fine(() -> "> newTask:created " + message.getTxid());

        this.innvocationTasks.put(task.getTxKey(), task);
        try {
            perflogger.fine(() -> "> newTask:submitting " + message.getTxid());

            // submit the task to run, with the taskService providing the
            // threading support.
            final CompletableFuture<Void> response = CompletableFuture.runAsync(() -> {
                task.call();
            }, taskService);

            // we have a future of the chaincode message that should be returned.
            // but waiting for this does not need to block this thread
            // it is important to wait for it however, as we need to remove it from the task
            // list
            response.thenRun(() -> {
                innvocationTasks.remove(task.getTxKey());
                perflogger.fine(() -> "< newTask:completed " + message.getTxid());
            });

            perflogger.fine(() -> "< newTask:submitted " + message.getTxid());

        } catch (final RejectedExecutionException e) {
            logger.warning(() -> "Failed to submit task " + message.getTxid() + Logging.formatError(e));
            // this means that there is no way that this can be handed off to another
            // thread for processing, and there's no space left in the queue to hold
            // it pending

            final ChaincodeMessage m = ChaincodeMessageFactory.newErrorEventMessage(message.getChannelId(), message.getTxid(),
                    "Failed to submit task for processing");
            this.outgoingMessage.accept(m);
        }

    }

    /**
     * Set the Consumer function to be used for sending messages back to the peer.
     *
     * @param outgoingMessage
     * @return InvocationTaskManager
     */
    public InvocationTaskManager setResponseConsumer(final Consumer<ChaincodeMessage> outgoingMessage) {
        this.outgoingMessage = outgoingMessage;

        return this;
    }

    /**
     * Send the initial protocol message for the 'register' phase.
     *
     * @return InvocationTaskManager
     */
    public InvocationTaskManager register() {

        logger.info(() -> "Registering new chaincode " + this.chaincodeId);
        chaincode.setState(ChaincodeBase.CCState.CREATED);
        this.outgoingMessage.accept(ChaincodeMessageFactory.newRegisterChaincodeMessage(this.chaincodeId));

        return this;
    }

    private static final int SHUTDOWN_TIMEOUT = 60;

    /**
     *
     */
    public void shutdown() {
        // Recommended shutdown process from
        // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
        // Disable new tasks from being submitted
        this.taskService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!taskService.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                taskService.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!taskService.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (final InterruptedException ex) {
            // (Re-)Cancel if current thread also interrupted
            taskService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
