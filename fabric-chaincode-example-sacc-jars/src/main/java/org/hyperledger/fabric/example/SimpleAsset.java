package org.hyperledger.fabric.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
@Contract
@Default
public class SimpleAsset implements ContractInterface {

    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param ctx {@link Context} to operate proposal and ledger
     * @param key key
     * @param value value
     */
    @Transaction
    public void init(Context ctx, String key, String value) {
        ctx.getStub().putStringState(key, value);
    }

    /**
     * get returns the value of the specified asset key
     *
     * @param ctx {@link Context} to operate proposal and ledger
     * @param key key
     * @return value
     */
    @Transaction
    public String get(Context ctx, String key) {
        String value = ctx.getStub().getStringState(key);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Asset not found: " + key);
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param ctx {@link Context} to operate proposal and ledger
     * @param key key
     * @param value value
     * @return value
     */
    @Transaction
    public String set(Context ctx, String key, String value) {
        ctx.getStub().putStringState(key, value);
        return value;
    }

}
