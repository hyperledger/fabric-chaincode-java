/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class ChaincodeTest {
    @Test
    public void testResponse() {
        final Chaincode.Response resp = new Chaincode.Response(
                Chaincode.Response.Status.SUCCESS, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertThat(Chaincode.Response.Status.SUCCESS).as("Incorrect status").isEqualTo(resp.getStatus());
        assertThat("No message").as("Incorrect message").isEqualTo(resp.getMessage());
        assertThat("no payload").as("Incorrect payload").isEqualTo(resp.getStringPayload());
    }

    @Test
    public void testResponseWithCode() {
        Chaincode.Response resp =
                new Chaincode.Response(200, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertThat(Chaincode.Response.Status.SUCCESS).as("Incorrect status").isEqualTo(resp.getStatus());
        assertThat(200).as("Incorrect status").isEqualTo(resp.getStatusCode());
        assertThat("No message").as("Incorrect message").isEqualTo(resp.getMessage());
        assertThat("no payload").as("Incorrect payload").isEqualTo(resp.getStringPayload());

        resp = new Chaincode.Response(404, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertThat(404).as("Incorrect status").isEqualTo(resp.getStatusCode());
        assertThat("No message").as("Incorrect message").isEqualTo(resp.getMessage());
        assertThat("no payload").as("Incorrect payload").isEqualTo(resp.getStringPayload());

        resp = new Chaincode.Response(
                Chaincode.Response.Status.ERROR_THRESHOLD, "No message", "no payload".getBytes(StandardCharsets.UTF_8));
        assertThat(Chaincode.Response.Status.ERROR_THRESHOLD)
                .as("Incorrect status")
                .isEqualTo(resp.getStatus());
        assertThat(400).as("Incorrect status").isEqualTo(resp.getStatusCode());
        assertThat("No message").as("Incorrect message").isEqualTo(resp.getMessage());
        assertThat("no payload").as("Incorrect payload").isEqualTo(resp.getStringPayload());
    }

    @Test
    public void testStatus() {
        assertThat(Chaincode.Response.Status.SUCCESS)
                .as("Wrong status")
                .isEqualTo(Chaincode.Response.Status.forCode(200));
        assertThat(Chaincode.Response.Status.ERROR_THRESHOLD)
                .as("Wrong status")
                .isEqualTo(Chaincode.Response.Status.forCode(400));
        assertThat(Chaincode.Response.Status.INTERNAL_SERVER_ERROR)
                .as("Wrong status")
                .isEqualTo(Chaincode.Response.Status.forCode(500));

        assertThatThrownBy(() -> Chaincode.Response.Status.forCode(501)).isInstanceOf(IllegalArgumentException.class);
    }
}
