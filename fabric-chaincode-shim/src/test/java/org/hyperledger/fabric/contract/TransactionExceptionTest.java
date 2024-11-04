/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.jupiter.api.Test;

final class TransactionExceptionTest {

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

    @Test
    void testNoArgConstructor() {
        final ChaincodeException e = new ChaincodeException();
        assertThat(e.getMessage(), is(nullValue()));
        assertThat(e.getPayload(), is(nullValue()));
    }

    @Test
    void testMessageArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure");
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(nullValue()));
    }

    @Test
    void testCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException(new Error("Cause"));
        assertThat(e.getMessage(), is("java.lang.Error: Cause"));
        assertThat(e.getPayload(), is(nullValue()));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    void testMessageAndCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(nullValue()));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    void testMessageAndPayloadArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'});
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
    }

    @Test
    void testMessagePayloadAndCauseArgConstructor() {
        final ChaincodeException e =
                new ChaincodeException("Failure", new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}, new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    void testMessageAndStringPayloadArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", "Payload");
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
    }

    @Test
    void testMessageStringPayloadAndCauseArgConstructor() {
        final ChaincodeException e = new ChaincodeException("Failure", "Payload", new Error("Cause"));
        assertThat(e.getMessage(), is("Failure"));
        assertThat(e.getPayload(), is(new byte[] {'P', 'a', 'y', 'l', 'o', 'a', 'd'}));
        assertThat(e.getCause().getMessage(), is("Cause"));
    }

    @Test
    void testSubclass() {
        final ChaincodeException e = new MyTransactionException(1);
        assertThat(e.getMessage(), is("MyTransactionException"));
        assertThat(e.getPayload(), is(new byte[] {'E', '0', '0', '1'}));
    }
}
