package org.hyperleder.fabric.example;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;

/**
 * SimpleAsset implements a simple chaincode to manage an asset
 */
@Contract(name = "MyAssetContract",
    info = @Info(title = "MyAsset contract",
                description = "Very basic Java Contract example",
                version = "0.0.1",
                license =
                        @License(name = "SPDX-License-Identifier: Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "MyAssetContract@example.com",
                                                name = "MyAssetContract",
                                                url = "http://MyAssetContract.me")))
@Default
public class SimpleAsset implements ContractInterface {

    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Transaction
    public void init(Context ctx) {
        // Set up any variables or assets here by calling stub.putState()
        // We store the key and the value on the ledger
        ctx.getStub().putStringState("a","100");

        for (int x=100; x<150; x++){
            ctx.getStub().putStringState("asset:"+x,"value:"+x);
        }

        System.out.println("Adding 50 assets");
    }


    /**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    @Transaction
    public String get(Context ctx, String key) {
        byte[] value = ctx.getStub().getState(key);
        if (value == null || value.length == 0) {
            throw new RuntimeException("Asset not found: " + key);
        }
        
        return new String(value, UTF_8);
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    @Transaction
    public void set(Context ctx, String key, String value) {
        ctx.getStub().putStringState(key,value);
    }

    @Transaction
    public void queryRange(Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String startKey = "asset:101";
        String endKey = "asset:121";

        QueryResultsIterator<KeyValue> results = stub.getStateByRange( startKey,  endKey);

        int i=1;
        for (KeyValue kv : results) {
            System.out.println(" "+(i++)+": "+kv.getKey() + " "  +  kv.getStringValue() );
        }

    }

    @Transaction
    public void queryRangePagination(Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String startKey = "asset:101";
        String endKey = "asset:121";

        QueryResultsIteratorWithMetadata<KeyValue> results = stub.getStateByRangeWithPagination( startKey,  endKey, 5, null);

        int i=1;
        for (KeyValue kv : results) {
            System.out.println(" "+(i++)+": "+kv.getKey() + " "  +  kv.getStringValue() );
        }

    }

}
