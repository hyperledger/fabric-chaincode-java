/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.shim.ledger.*;
import org.hyperledger.fabric.shim.*;

import java.util.*;
import static java.nio.charset.StandardCharsets.UTF_8;

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