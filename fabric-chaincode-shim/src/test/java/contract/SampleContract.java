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
import org.hyperledger.fabric.shim.ChaincodeException;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Contract(name = "samplecontract", info = @Info(contact = @Contact(email = "fred@example.com")))
@Default()
public class SampleContract implements ContractInterface {
    static public int beforeInvoked = 0;
    static public int afterInvoked = 0;
    static public int doWorkInvoked = 0;
    static public int t1Invoked = 0;
    static public int i1Invoked = 0;

    @Transaction
    public String t5(Context ctx) {
        doSomeWork();
        System.out.println("SampleContract::T5 Done");
        return null;
    }

    @Transaction(name = "t4")
    public String tFour(Context ctx) {

        System.out.println("SampleContract::T4 Done");
        return "Transaction 4";
    }

    @Transaction
    public String t3(Context ctx, String exception, String message) {
        if ("TransactionException".equals(exception)) {
            if (message.isEmpty()) {
                throw new ChaincodeException(null, "T3ERR1");
            } else {
                throw new ChaincodeException(message, "T3ERR1");
            }
        } else {
            throw new RuntimeException(message);
        }
    }

    @Transaction
    public String t2(Context ctx) {

        System.out.println("SampleContract::T2 Done");
        return "Transaction 2";
    }

    @Transaction
    public void noReturn(Context ctx) {
        System.out.println("SampleContract::noReturn done");
    }

    @Transaction
    public String t1(Context ctx, String arg1) {
        t1Invoked++;

        List<String> args = ctx.getStub().getStringArgs();
        doSomeWork();
        System.out.println("SampleContract::T1 Done");
        return args.get(1);
    }

    @Override
    public void beforeTransaction(Context ctx) {
        beforeInvoked++;
    }

    @Override
    public void afterTransaction(Context ctx, Object value) {
        afterInvoked++;
    }

    private void doSomeWork() {
        doWorkInvoked++;
    }
}
