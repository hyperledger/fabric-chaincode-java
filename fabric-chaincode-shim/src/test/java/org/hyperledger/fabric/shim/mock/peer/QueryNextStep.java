/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.mock.peer;

import org.hyperledger.fabric.protos.peer.ChaincodeShim;

/**
 * Simulates requesting/receiving next set of results for query
 * Waits for QUERY_STATE_NEXT
 * Returns message that contains list of results in form ("key" => "key Value")*
 */
public class QueryNextStep extends QueryResultStep {

    /**
     * Initiate step
     * @param hasNext is response message QueryResponse hasMore field set
     * @param vals list of keys to generate ("key" => "key Value") pairs
     */
    public QueryNextStep(boolean hasNext, String... vals) {
        super(hasNext, vals);
    }

    @Override
    public boolean expected(ChaincodeShim.ChaincodeMessage msg) {
        super.orgMsg = msg;
        return msg.getType() == ChaincodeShim.ChaincodeMessage.Type.QUERY_STATE_NEXT;
    }
}
