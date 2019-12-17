/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.utils;

import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;

import com.google.protobuf.ByteString;

public final class MessageUtil {

    private MessageUtil() {

    }

    /**
     * Generate chaincode messages
     *
     * @param type
     * @param channelId
     * @param txId
     * @param payload
     * @param event
     * @return
     */
    public static ChaincodeShim.ChaincodeMessage newEventMessage(final ChaincodeShim.ChaincodeMessage.Type type, final String channelId, final String txId,
            final ByteString payload, final ChaincodeEventPackage.ChaincodeEvent event) {
        final ChaincodeShim.ChaincodeMessage.Builder builder = ChaincodeShim.ChaincodeMessage.newBuilder()
                .setType(type)
                .setChannelId(channelId)
                .setTxid(txId)
                .setPayload(payload);
        if (event != null) {
            builder.setChaincodeEvent(event);
        }
        return builder.build();
    }
}
