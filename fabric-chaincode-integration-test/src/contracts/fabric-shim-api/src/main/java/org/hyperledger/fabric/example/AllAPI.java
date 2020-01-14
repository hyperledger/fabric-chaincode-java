/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;


import org.hyperledger.fabric.shim.ledger.*;
import org.hyperledger.fabric.shim.*;
import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "AllAPI",
    info = @Info(title = "AllAPI contract",
                description = "Contract but using all the APIs",
                version = "0.0.1",
                license =
                        @License(name = "SPDX-License-Identifier: Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "fred@example.com",
                                                name = "fred",
                                                url = "http://fred.example.com")))
@Default
public class AllAPI implements ContractInterface {
    public AllAPI() {

    }

    @Transaction()
    public void putBulkStates(Context ctx){
        for (int x=100; x<200; x++){
            String key = "key"+x;
            String value = "value:"+x;

            putState(ctx,key,value);
        }
    }

    @Transaction()
    public void putState(Context ctx, String key, String payload){
        ChaincodeStub stub = ctx.getStub();
        stub.putState(key,payload.getBytes(UTF_8));
    }

    @Transaction()
    public void putStateComposite(Context ctx, String key[], String payload){
        String composite = new CompositeKey("composite",key).toString();
        this.putState(ctx,composite,payload);
    }

    @Transaction()
    public void getState(Context ctx, String key, String payload){
        ChaincodeStub stub = ctx.getStub();
        String result = stub.getStringState(key);
        if (!result.equals(payload)){
            String msg = "GetState::["+key+"] Expected "+payload+" got "+result;
            System.out.println(msg);
            throw new RuntimeException(msg);
        }
    }

    @Transaction()
    public int getByRange(Context ctx, String start, String end){
        ChaincodeStub stub = ctx.getStub();
        System.out.println("getByRange>>");
        QueryResultsIterator<KeyValue> qri = stub.getStateByRange(start,end);
        int count=0;
        for (KeyValue kv : qri){
            kv.getKey();
            kv.getStringValue();
            count++;
            System.out.println("["+kv.getKey()+"] "+kv.getStringValue());
        }
        System.out.println("getByRange<<");
        return count;
    }

    @Transaction()
    public String getByRangePaged(Context ctx, String start, String end, int pageSize, String bookmark){
        ChaincodeStub stub = ctx.getStub();
        System.out.println("getByRangePaged>>");
        QueryResultsIteratorWithMetadata<KeyValue> qri = stub.getStateByRangeWithPagination(start,end,pageSize,bookmark);
        for (KeyValue kv : qri){
            kv.getKey();
            kv.getStringValue();
            System.out.println("["+kv.getKey()+"] "+kv.getStringValue());
        }

        String newbookmark = qri.getMetadata().getBookmark();
        int records = qri.getMetadata().getFetchedRecordsCount();
        System.out.println(newbookmark+" @ "+records);

        System.out.println("getByRangePaged<<");
        return newbookmark;
    }

}