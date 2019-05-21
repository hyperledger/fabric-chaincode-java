/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package contract;

import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Contract(
        namespace = "samplecontract",
        info = @Info(
                contact = @Contact( email = "fred@example.com" )
        )
)
@Default()
public class SampleContract implements ContractInterface {
    static public int beforeInvoked = 0;
    static public int afterInvoked = 0;
    static public int doWorkInvoked = 0;
    static public int t1Invoked = 0;
    static public int i1Invoked = 0;

    @Transaction
    public String t3() {
    	throw new RuntimeException("T3 fail!");
    }

    @Transaction
    public String t2() {

        System.out.println("SampleContract::T2 Done");
        return "Transaction 2";
    }

    @Transaction
    public String t1(String arg1) {
        t1Invoked++;
        Context context = getContext();
        List<String> args = context.getStringArgs();
        doSomeWork();
        System.out.println("SampleContract::T1 Done");
        return args.get(1);
    }

    @Override
    public void beforeTransaction() {
        beforeInvoked++;
    }

    @Override
    public void afterTransaction() {
        afterInvoked++;
    }
    private void doSomeWork() {
        doWorkInvoked++;
    }
}
