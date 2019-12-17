/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim.mock.peer;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

/**
 * Simulates query invocation. Waits for GET_QUERY_RESULT Returns message that
 * contains list of results in form ("key" => "key Value")*
 */
public final class GetQueryResultStep extends QueryResultStep {

    /**
     * Initiate step
     *
     * @param hasNext is response message QueryResponse hasMore field set
     * @param vals    list of keys to generate ("key" => "key Value") pairs
     */
    public GetQueryResultStep(final boolean hasNext, final String... vals) {
        super(hasNext, vals);
    }

    @Override
    public boolean expected(final ChaincodeShim.ChaincodeMessage msg) {
        super.orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.GET_QUERY_RESULT;
    }

}
