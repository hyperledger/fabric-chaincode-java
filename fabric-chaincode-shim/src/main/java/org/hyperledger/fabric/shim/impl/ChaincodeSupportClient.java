/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc.ChaincodeSupportStub;

import java.util.concurrent.TimeUnit;

public class ChaincodeSupportClient {

	private final ManagedChannel channel;
	private final ChaincodeSupportStub stub;

	public ChaincodeSupportClient(ManagedChannelBuilder<?> channelBuilder) {
		this.channel = channelBuilder.build();
		this.stub = ChaincodeSupportGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
		this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public StreamObserver<ChaincodeMessage> register(StreamObserver<ChaincodeMessage> responseObserver) {
		return stub.register(responseObserver);
	}

}
