/*
 * Copyright IBM Corp., DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.protos.peer.ChaincodeSupportGrpc;
import org.hyperledger.fabric.shim.Chaincode;

import static java.lang.String.format;

public class ChatStream implements StreamObserver<ChaincodeShim.ChaincodeMessage> {

	private static final Log logger = LogFactory.getLog(ChatStream.class);

	private final ManagedChannel connection;
	private final Handler handler;
	private StreamObserver<ChaincodeShim.ChaincodeMessage> streamObserver;

	public ChatStream(ManagedChannel connection, Chaincode chaincode) {
		// Establish stream with validating peer
		ChaincodeSupportGrpc.ChaincodeSupportStub stub = ChaincodeSupportGrpc.newStub(connection);

		logger.info("Connecting to peer.");

		try {
			this.streamObserver = stub.register(this);
		} catch (Exception e) {
			logger.error("Unable to connect to peer server", e);
			System.exit(-1);
		}
		this.connection = connection;

		// Create the org.hyperledger.fabric.shim handler responsible for all
		// control logic
		this.handler = new Handler(this, chaincode);
	}

	public synchronized void serialSend(ChaincodeShim.ChaincodeMessage message) {
		if(logger.isDebugEnabled()) {
			logger.debug(format("[%-8s]Sending %s message to peer.", message.getTxid(), message.getType()));
		}
		if (logger.isTraceEnabled()) {
			logger.trace(format("[%-8s]ChaincodeMessage: %s", message.getTxid(), toJsonString(message)));
		}
		try {
			this.streamObserver.onNext(message);
			if (logger.isTraceEnabled()) {
				logger.trace(format("[%-8s]%s message sent.", message.getTxid(), message.getType()));
			}
		} catch (Exception e) {
			logger.error(String.format("[%-8s]Error sending %s: %s", message.getTxid(), message.getType(), e));
			throw new RuntimeException(format("Error sending %s: %s", message.getType(), e));
		}
	}

	@Override
	public void onNext(ChaincodeShim.ChaincodeMessage message) {
		if(logger.isDebugEnabled()) {
			logger.debug("Got message from peer: " + toJsonString(message));
		}
		try {
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("[%-8s]Received message %s from org.hyperledger.fabric.shim", message.getTxid(), message.getType()));
			}
			handler.handleMessage(message);
		} catch (Exception e) {
			logger.error(String.format("[%-8s]Error handling message %s: %s", message.getTxid(), message.getType(), e));
			System.exit(-1);
		}
	}

	@Override
	public void onError(Throwable e) {
		logger.error("Unable to connect to peer server: " + e.getMessage(), e);
		System.exit(-1);
	}

	@Override
	public void onCompleted() {
		connection.shutdown();
		handler.nextState.close();
	}

	static String toJsonString(ChaincodeShim.ChaincodeMessage message) {
		try {
			return JsonFormat.printer().print(message);
		} catch (InvalidProtocolBufferException e) {
			return String.format("{ Type: %s, TxId: %s }", message.getType(), message.getTxid());
		}
	}

	public void receive() throws Exception {
		NextStateInfo nsInfo = handler.nextState.take();
		ChaincodeShim.ChaincodeMessage message = nsInfo.message;
		onNext(message);

		// keepalive messages are PONGs to the fabric's PINGs
		if (nsInfo.sendToCC || message.getType() == ChaincodeShim.ChaincodeMessage.Type.KEEPALIVE) {
			if (message.getType() == ChaincodeShim.ChaincodeMessage.Type.KEEPALIVE) {
				logger.info("Sending KEEPALIVE response");
			} else {
				logger.info(String.format("[%-8s]Send state message %s", message.getTxid(), message.getType()));
			}
			serialSend(message);
		}
	}
}
