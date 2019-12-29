/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TransactionExceptionTest {

    class MyTransactionException extends ChaincodeException {

        private static final long serialVersionUID = 1L;

        private final int errorCode;

        MyTransactionException(final int errorCode) {
            super("MyTransactionException");
            this.errorCode = errorCode;
        }

        @Override
        public byte[] getPayload() {
            final String payload = String.format("E%03d", errorCode);
            return payload.getBytes();
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNoArgConstructor() {
        final ChaincodeException e = new ChaincodeException();
        assertThat(e.getMessage(), is(nullValue()));
        assertThat(e.getPayload(), is(nullValue()));
    }

    @Test
    public void testMessageArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure");
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(nullValue()));
    }

    @Test
    public void testCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException(new Error("Cause"));
        assertThat(e.getMessage(), is("java.lang.Error: Cause"));
        assertThat(e.getPayload(), is(nullValue()));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    public void testMessageAndCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(nullValue()));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    public void testMessageAndPayloadArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'});
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
    }

    @Test
    public void testMessagePayloadAndCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}, new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    public void testMessageAndStringPayloadArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", "Payload");
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
    }

    @Test
    public void testMessageStringPayloadAndCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", "Payload", new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    public void testSubclass() {
        final ChaincodeException e = new MyTransactionException(1);
        assertThat(e.getMessage(), is("MyTransactionException"));
        assertThat(e.getPayload(), is(new byte[] {'E', '0', '0', '1'}));
    }
}
