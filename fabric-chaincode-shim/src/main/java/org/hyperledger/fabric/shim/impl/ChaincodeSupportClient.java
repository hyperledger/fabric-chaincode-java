/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc.ChaincodeSupportStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ChaincodeSupportClient {
	private static Logger logger = Logging.getLogger(ChaincodeSupportClient.class);
	private static Logger perflogger = Logging.getLogger("org.hyperledger.performance");

	private final ManagedChannel channel;
	private final ChaincodeSupportStub stub;

	public ChaincodeSupportClient(ManagedChannelBuilder<?> channelBuilder) {
		this.channel = channelBuilder.build();
		this.stub = ChaincodeSupportGrpc.newStub(channel);
	}

	private void shutdown(InnvocationTaskManager itm) {

		// first shutdown the thread pool
		itm.shutdown();
		try {
			this.channel.shutdown();
			if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
				channel.shutdownNow();
				if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
					System.err.println("Channel did not terminate");
				}
			};
		} catch (InterruptedException e) {
			channel.shutdownNow();
			Thread.currentThread().interrupt();
		}

	}

	public void start(InnvocationTaskManager itm) {

		// the response stream is the message flow FROM the peer
		// the request observer is the message flow TO the peer

		// route the messages from the peer to the InnvocationTaskManager, to be handled
		// to the
		// correct Task for processing.
		Consumer<ChaincodeMessage> consumer = itm::onChaincodeMessage;

		logger.info("making the grpc call");
		StreamObserver<ChaincodeMessage> requestObserver = this.stub.register(

				new StreamObserver<ChaincodeMessage>() {
					@Override
					public void onNext(ChaincodeMessage chaincodeMessage) {
						consumer.accept(chaincodeMessage);
					}

					@Override
					public void onError(Throwable t) {
						logger.severe(()->"An error occured on the chaincode stream. Shutting down the chaincode stream."
								+ Logging.formatError(t));

						ChaincodeSupportClient.this.shutdown(itm);
					}

					@Override
					public void onCompleted() {
						logger.severe("Chaincode stream is complete. Shutting down the chaincode stream.");
						ChaincodeSupportClient.this.shutdown(itm);
					}
				}

		);

		// Consumer function for response messages
		Consumer<ChaincodeMessage> c = new Consumer<ChaincodeMessage>() {

			// create a lock, with fair property
			ReentrantLock lock = new ReentrantLock(true);

			@Override
			public void accept(ChaincodeMessage t) {
				lock.lock();
				perflogger.fine(()->"> sendToPeer "+t.getTxid());
				requestObserver.onNext(t);
				perflogger.fine(()->"< sendToPeer "+t.getTxid());
				lock.unlock();
			}
		};

		// Pass a Consumer interface back to the the task manager. This is for tasks to use to respond back to the peer.
		itm.setResponseConsumer(c).register();

	}
}
