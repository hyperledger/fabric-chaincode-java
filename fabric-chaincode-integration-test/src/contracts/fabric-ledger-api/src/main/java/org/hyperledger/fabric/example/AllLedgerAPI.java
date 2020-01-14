/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.example;



import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;


import org.hyperledger.fabric.ledger.*;

import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "AllLedgerAPI",
    info = @Info(title = "AllLedgerAPI contract",
                description = "Contract but using all the Ledger APIs",
                version = "0.0.1",
                license =
                        @License(name = "SPDX-License-Identifier: Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "fred@example.com",
                                                name = "fred",
                                                url = "http://fred.example.com")))
@Default
public class AllLedgerAPI implements ContractInterface {
    
    public AllLedgerAPI() {

    }

    @Transaction()
    public void accessLedgers(Context ctx){
        Ledger ledger = Ledger.getLedger(ctx);
        
        // not much else can be done for the moment
    }

    
}