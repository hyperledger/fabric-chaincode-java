/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package contract;

import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

@Contract(
        name = "samplecontract",
        info = @Info(
                contact = @Contact(
                        email = "fred@example.com"),
                license = @License(
                        name = "fred",
                        url = "http://fred.me"),
                version = "0.0.1",
                title = "samplecontract"))
@Default()
public class SampleContract implements ContractInterface {
    public static int getBeforeInvoked() {
        return beforeInvoked;
    }

    public static int getAfterInvoked() {
        return afterInvoked;
    }

    public static int getDoWorkInvoked() {
        return doWorkInvoked;
    }

    public static int getT1Invoked() {
        return t1Invoked;
    }

    public static int getI1Invoked() {
        return i1Invoked;
    }

    public static void setBeforeInvoked(final int beforeInvoked) {
        SampleContract.beforeInvoked = beforeInvoked;
    }

    public static void setAfterInvoked(final int afterInvoked) {
        SampleContract.afterInvoked = afterInvoked;
    }

    public static void setDoWorkInvoked(final int doWorkInvoked) {
        SampleContract.doWorkInvoked = doWorkInvoked;
    }

    public static void setT1Invoked(final int t1Invoked) {
        SampleContract.t1Invoked = t1Invoked;
    }

    public static void setI1Invoked(final int i1Invoked) {
        SampleContract.i1Invoked = i1Invoked;
    }

    private static int beforeInvoked = 0;
    private static int afterInvoked = 0;
    private static int doWorkInvoked = 0;
    private static int t1Invoked = 0;
    private static int i1Invoked = 0;

    /**
     * @param ctx
     * @return
     */
    @Transaction
    public String t5(final Context ctx) {
        doSomeWork();
        System.out.println("SampleContract::T5 Done");
        return null;
    }

    /**
     * @param ctx
     * @return
     */
    @Transaction(name = "t4")
    public String tFour(final Context ctx) {

        System.out.println("SampleContract::T4 Done");
        return "Transaction 4";
    }

    /**
     * @param ctx
     * @param exception
     * @param message
     * @return
     */
    @Transaction
    public String t3(final Context ctx, final String exception, final String message) {
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

    /**
     * @param ctx
     * @return
     */
    @Transaction
    public String t2(final Context ctx) {

        System.out.println("SampleContract::T2 Done");
        return "Transaction 2";
    }

    /**
     * @param ctx
     */
    @Transaction
    public void noReturn(final Context ctx) {
        System.out.println("SampleContract::noReturn done");
    }

    /**
     * @param ctx
     * @param arg1
     * @return
     */
    @Transaction
    public String t1(final Context ctx, final String arg1) {
        t1Invoked++;

        final List<String> args = ctx.getStub().getStringArgs();
        doSomeWork();
        System.out.println("SampleContract::T1 Done");
        return args.get(1);
    }

    /**
     *
     */
    @Override
    public void beforeTransaction(final Context ctx) {
        beforeInvoked++;
    }

    /**
     *
     */
    @Override
    public void afterTransaction(final Context ctx, final Object value) {
        afterInvoked++;
    }

    private void doSomeWork() {
        doWorkInvoked++;
    }
}
