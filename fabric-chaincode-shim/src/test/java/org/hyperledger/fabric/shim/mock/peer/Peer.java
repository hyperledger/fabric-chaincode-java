/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.COMPLETED;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.PUT_STATE;
import static org.hyperledger.fabric.protos.peer.ChaincodeShim.ChaincodeMessage.Type.TRANSACTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.utils.MessageUtil;

public class Peer {

    public static void main(final String[] args) throws Throwable {
        final Scanner scan = new Scanner(System.in);
        System.out.println("Hello Peer");
        ChaincodeMockPeer server;

        long timpForAllSends = 0;

        final int tx_batch = 50000;
        final List<ScenarioStep> scenario = new ArrayList<>();
        scenario.add(new RegisterStep());
        for (int x = 0; x < tx_batch; x++) {
            scenario.add(new PutValueStep("key1"));
        }

        // final ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
        // .addArgs(ByteString.copyFromUtf8("createAsset"))
        // .addArgs(ByteString.copyFromUtf8("key1"))
        // .addArgs(ByteString.copyFromUtf8("key1"))
        // .build().toByteString();
        // final ChaincodeShim.ChaincodeMessage invokeMsg =
        // MessageUtil.newEventMessage(TRANSACTION, "testChannel", "0", invokePayload,
        // null);

        System.out.println("Starting...");
        server = ChaincodeMockPeer.startServer(scenario);
        System.out.println("Started");

        ChaincodeMockPeer.checkScenarioStepEnded(server, 1, 50000, TimeUnit.MILLISECONDS);
        System.out.println("invoking...");
        Thread.sleep(2000);
        final long start = System.nanoTime();
        for (int x = 0; x < tx_batch; x++) {
            final ByteString invokePayload = Chaincode.ChaincodeInput.newBuilder()
                    // .addArgs(ByteString.copyFromUtf8("emptyContract"))
                    .addArgs(ByteString.copyFromUtf8("createAsset")).addArgs(ByteString.copyFromUtf8("key" + x))
                    .addArgs(ByteString.copyFromUtf8("key1")).build().toByteString();
            final ChaincodeShim.ChaincodeMessage invokeMsg = MessageUtil.newEventMessage(TRANSACTION, "testChannel",
                    String.format("txid::%03d", x), invokePayload, null);

            final long bi = System.nanoTime();
            server.send(invokeMsg);
            final long ai = System.nanoTime();
            timpForAllSends += (ai - bi);
            // if (x % 2 == 0){
            Thread.sleep(1);
            // }

        }
        final long endsend = System.nanoTime();
        System.out.println("Enter to continue, sent " + tx_batch);
        scan.nextLine();
        final long enter = System.nanoTime();
        final long durationSend = (endsend - start);

        final long finishOff = (enter - endsend);

        final ArrayList<ChaincodeShim.ChaincodeMessage> msgs = server.getAllReceivedMessages();
        System.out.println(msgs);
        final List<ChaincodeShim.ChaincodeMessage> filteredMsgs = msgs.stream()
                .filter(msg -> msg.getType().equals(PUT_STATE)).collect(Collectors.toList());
        System.out.println("PUT_STATE = " + filteredMsgs.size());
        final List<String> completedtxids = msgs.stream().filter(msg -> msg.getType().equals(COMPLETED))
                .map(e -> e.getTxid())
                .sorted().collect(Collectors.toList());
        System.out.println("COMPLETED = " + completedtxids.size());
        System.out.println("COMPLETED = " + completedtxids);

        System.out.println("Duration all send=" + durationSend / 1000000 + "ms   Finish off=" + finishOff / 1000000);
        System.out.println("Duration per send=" + (timpForAllSends / tx_batch) + "ns tps="
                + (durationSend / 1000000000) / tx_batch);
    }
}