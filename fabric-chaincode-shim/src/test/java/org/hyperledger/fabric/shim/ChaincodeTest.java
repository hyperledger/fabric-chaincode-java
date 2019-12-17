/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChaincodeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testResponse() {
        final Chaincode.Response resp = new Chaincode.Response(Chaincode.Response.Status.SUCCESS, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertEquals("Incorrect status", Chaincode.Response.Status.SUCCESS, resp.getStatus());
        assertEquals("Incorrect message", "No message", resp.getMessage());
        assertEquals("Incorrect payload", "no payload", resp.getStringPayload());
    }

    @Test
    public void testResponseWithCode() {
        Chaincode.Response resp = new Chaincode.Response(200, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertEquals("Incorrect status", Chaincode.Response.Status.SUCCESS, resp.getStatus());
        assertEquals("Incorrect status", 200, resp.getStatusCode());
        assertEquals("Incorrect message", "No message", resp.getMessage());
        assertEquals("Incorrect payload", "no payload", resp.getStringPayload());

        resp = new Chaincode.Response(404, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertEquals("Incorrect status", 404, resp.getStatusCode());
        assertEquals("Incorrect message", "No message", resp.getMessage());
        assertEquals("Incorrect payload", "no payload", resp.getStringPayload());

        resp = new Chaincode.Response(Chaincode.Response.Status.ERROR_THRESHOLD, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertEquals("Incorrect status", Chaincode.Response.Status.ERROR_THRESHOLD, resp.getStatus());
        assertEquals("Incorrect status", 400, resp.getStatusCode());
        assertEquals("Incorrect message", "No message", resp.getMessage());
        assertEquals("Incorrect payload", "no payload", resp.getStringPayload());
    }

    @Test
    public void testStatus() {
        assertEquals("Wrong status", Chaincode.Response.Status.SUCCESS, Chaincode.Response.Status.forCode(200));
        assertEquals("Wrong status", Chaincode.Response.Status.ERROR_THRESHOLD, Chaincode.Response.Status.forCode(400));
        assertEquals("Wrong status", Chaincode.Response.Status.INTERNAL_SERVER_ERROR, Chaincode.Response.Status.forCode(500));

        thrown.expect(IllegalArgumentException.class);
        Chaincode.Response.Status.forCode(501);
    }
}
