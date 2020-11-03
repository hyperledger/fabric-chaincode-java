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

@Contract(name = "LedgerAPI",
    info = @Info(title = "Ledger API Examples",
                description = "Technical Details and use of the Ledger API",
                version = "0.0.1",
                license =
                        @License(name = "SPDX-License-Identifier: Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "ledgerapi@example.com",
                                                name = "ledgerapi",
                                                url = "http://ledgerapi.me")))
@Default
public class LedgerAPIContract implements ContractInterface {

    public  LedgerAPIContract() {

    }


    /**
     * Example showing getting the world state ledger.
     */
    @Transaction()
    public boolean assetExistsInWorldState(Context ctx, String myAssetId) {
        Collection collection = Ledger.getLedger(ctx).getCollection(Collection.WORLD);
        // check exists
        return false;
    }

    public final static String PRIVATE_RECORDS = "PrivateRecords";

    /**
     * Example showing getting the a named private data collectionm.
     */
    @Transaction()
    public boolean assetExistsInPrivateData(Context ctx, String myAssetId) {
        Collection collection = Ledger.getLedger(ctx).getCollection(LedgerAPIContract.PRIVATE_RECORDS);

        // check exists
        return false;
    }

    /** 
     * Exanmple showing getting one of the organizational collections
     */
    @Transaction()
    public boolean assetExistsInOrganizationPrivateData(Context ctx, String myAssetId) {
        Collection collection = Ledger.getLedger(ctx).getCollection(Collection.organizationCollection("myMspId"));

        // check exists
        return false;
    }

}