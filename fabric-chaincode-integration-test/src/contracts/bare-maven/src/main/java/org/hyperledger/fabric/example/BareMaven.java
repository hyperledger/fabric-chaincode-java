/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(name = "BareMaven",
    info = @Info(title = "BareGradle contract",
                description = "Contract but using all the APIs",
                version = "0.0.1",
                license =
                        @License(name = "SPDX-License-Identifier: Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "fred@example.com",
                                                name = "fred",
                                                url = "http://fred.example.com")))
@Default
public class BareMaven implements ContractInterface {
    public BareMaven() {

    }

    @Transaction()
    public String whoami(Context ctx){
       return this.getClass().getSimpleName();
    }
}