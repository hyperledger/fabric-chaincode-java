/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChaincodeSupportStream {

	private final Logger logger = Logger.getLogger(ChaincodeSupportStream.class.getName());
	private final ChaincodeSupportClient chaincodeSupportClient;
	private final Consumer<ChaincodeMessage> consumer;
	private final Supplier<ChaincodeMessage> supplier;
	private final StreamObserver<ChaincodeMessage> requestObserver;
	private final StreamObserver<ChaincodeMessage> responseObserver = new StreamObserver<ChaincodeMessage>() {
		@Override
		public void onNext(ChaincodeMessage chaincodeMessage) {
			consumer.accept(chaincodeMessage);
		}
		@Override
		public void onError(Throwable t) {
			logger.log(Level.SEVERE, "An error occured on the chaincode stream. Shutting down the chaincode stream.", t);
			ChaincodeSupportStream.this.shutdown();
		}
		@Override
		public void onCompleted() {
			logger.info("Chaincode stream is shutting down.");
			ChaincodeSupportStream.this.shutdown();
		}
	};
	final private Thread supplierComsumptionThread = new Thread() {
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				ChaincodeSupportStream.this.requestObserver.onNext(ChaincodeSupportStream.this.supplier.get());
			}
		}
	};

	public ChaincodeSupportStream(ManagedChannelBuilder<?> channelBuilder, Consumer<ChaincodeMessage> consumer, Supplier<ChaincodeMessage> supplier) {
		this.chaincodeSupportClient = new ChaincodeSupportClient(channelBuilder);
		this.consumer = consumer;
		this.requestObserver = this.chaincodeSupportClient.register(this.responseObserver);
		this.supplier = supplier;
		this.supplierComsumptionThread.start();
	}

	private void shutdown() {
		this.supplierComsumptionThread.interrupt();
		try {
			this.chaincodeSupportClient.shutdown();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
