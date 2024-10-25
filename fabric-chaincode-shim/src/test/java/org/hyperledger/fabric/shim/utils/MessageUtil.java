/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.utils;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.peer.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ChaincodeMessage;

public final class MessageUtil {

    private MessageUtil() {}

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
    public static ChaincodeMessage newEventMessage(
            final ChaincodeMessage.Type type,
            final String channelId,
            final String txId,
            final ByteString payload,
            final ChaincodeEvent event) {
        final ChaincodeMessage.Builder builder = ChaincodeMessage.newBuilder()
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
