/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;

import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

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
public class MyAssetContract implements ContractInterface {
    public  MyAssetContract() {

    }
    @Transaction()
    public boolean myAssetExists(Context ctx, String myAssetId) {
        byte[] buffer = ctx.getStub().getState(myAssetId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void createMyAsset(Context ctx, String myAssetId, String value) {
        boolean exists = myAssetExists(ctx,myAssetId);
        if (exists) {
            throw new RuntimeException("The asset "+myAssetId+" already exists");
        }
        MyAsset asset = new MyAsset();
        asset.setValue(value);
        ctx.getStub().putState(myAssetId, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public MyAsset readMyAsset(Context ctx, String myAssetId) {
        boolean exists = myAssetExists(ctx,myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset "+myAssetId+" does not exist");
        }

        MyAsset newAsset = MyAsset.fromJSONString(new String(ctx.getStub().getState(myAssetId),UTF_8));
        return newAsset;
    }

    @Transaction()
    public void updateMyAsset(Context ctx, String myAssetId, String newValue) {
        boolean exists = myAssetExists(ctx,myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset "+myAssetId+" does not exist");
        }
        MyAsset asset = new MyAsset();
        asset.setValue(newValue);

        ctx.getStub().putState(myAssetId, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public void deleteMyAsset(Context ctx, String myAssetId) {
        boolean exists = myAssetExists(ctx,myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset "+myAssetId+" does not exist");
        }
        ctx.getStub().delState(myAssetId);
    }

}