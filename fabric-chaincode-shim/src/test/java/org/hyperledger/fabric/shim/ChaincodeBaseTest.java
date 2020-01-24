/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.Matchers;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import io.grpc.ManagedChannelBuilder;

public class ChaincodeBaseTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNewSuccessResponseEmpty() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse();
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertNull("Response message in not null", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewSuccessResponseWithMessage() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse("Simple message");
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewSuccessResponseWithPayload() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse("Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertNull("Response message in not null", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(), "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewSuccessResponseWithMessageAndPayload() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse("Simple message",
                "Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(), "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseEmpty() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse();
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertNull("Response message in not null", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithMessage() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse("Simple message");
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithPayload() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse("Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertNull("Response message in not null", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(), "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseWithMessageAndPayload() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse("Simple message",
                "Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(), "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseWithException() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse(new Exception("Simple exception"));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message is not correct", "Unexpected error", response.getMessage());
        assertNull("Response payload is not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithChaincodeException() {
        final org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse(new ChaincodeException("Chaincode exception"));
        assertEquals("Response status is incorrect", response.getStatus(), org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message is not correct", "Chaincode exception", response.getMessage());
        assertNull("Response payload is not null", response.getPayload());
    }

    @Test
    public void testOptions() throws Exception {
        final ChaincodeBase cb = new EmptyChaincode();

        assertEquals("Host incorrect", ChaincodeBase.DEFAULT_HOST, cb.getHost());
        assertEquals("Port incorrect", ChaincodeBase.DEFAULT_PORT, cb.getPort());
        assertFalse("TLS should not be enabled", cb.isTlsEnabled());

        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "non_exist_path3");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "non_exist_path2");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "non_exist_path1");
        cb.processEnvironmentOptions();
        assertEquals("CCId incorrect", cb.getId(), "mycc");
        assertEquals("Host incorrect", cb.getHost(), "localhost");
        assertEquals("Port incorrect", cb.getPort(), 7052);
        assertTrue("TLS should be enabled", cb.isTlsEnabled());
        assertEquals("Root certificate file", "non_exist_path1", cb.getTlsClientRootCertPath());
        assertEquals("Client key file", "non_exist_path2", cb.getTlsClientKeyPath());
        assertEquals("Client certificate file", "non_exist_path3", cb.getTlsClientCertPath());

        environmentVariables.set("CORE_PEER_ADDRESS", "localhost1");
        cb.processEnvironmentOptions();
        assertEquals("Host incorrect", cb.getHost(), "localhost");
        assertEquals("Port incorrect", cb.getPort(), 7052);

        try {
            cb.validateOptions();
        } catch (final IllegalArgumentException e) {
            fail("Wrong arguments");
        }

        cb.processCommandLineOptions(new String[] {"-i", "mycc1", "--peerAddress", "localhost.org:7053"});
        assertEquals("CCId incorrect", cb.getId(), "mycc1");
        assertEquals("Host incorrect", cb.getHost(), "localhost.org");
        assertEquals("Port incorrect", cb.getPort(), 7053);

        try {
            cb.validateOptions();
        } catch (final IllegalArgumentException e) {
            fail("Wrong arguments");
        }

        cb.processCommandLineOptions(new String[] {"-i", "mycc1", "--peerAddress", "localhost1.org.7054"});
        assertEquals("Host incorrect", cb.getHost(), "localhost.org");
        assertEquals("Port incorrect", cb.getPort(), 7053);
    }

    @Test
    public void testUnsetOptionId() {
        final ChaincodeBase cb = new EmptyChaincode();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(Matchers.containsString("The chaincode id must be specified"));
        cb.validateOptions();
    }

    @Test
    public void testUnsetOptionClientCertPath() {
        final ChaincodeBase cb = new EmptyChaincode();
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        cb.processEnvironmentOptions();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(Matchers.containsString("Client key certificate chain"));
        cb.validateOptions();
    }

    @Test
    public void testUnsetOptionClientKeyPath() {
        final ChaincodeBase cb = new EmptyChaincode();
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "non_exist_path3");
        cb.processEnvironmentOptions();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(Matchers.containsString("Client key ("));
        cb.validateOptions();
    }

    @Test
    @Ignore
    public void testNewChannelBuilder() throws Exception {
        final ChaincodeBase cb = new EmptyChaincode();

        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        cb.processEnvironmentOptions();
        cb.validateOptions();
        assertTrue("Not correct builder", cb.newChannelBuilder() instanceof ManagedChannelBuilder);
    }

    @Test
    public void testInitializeLogging() {
        final ChaincodeBase cb = new EmptyChaincode();

        cb.processEnvironmentOptions();
        cb.initializeLogging();
        assertEquals("Wrong log level for org.hyperledger.fabric.shim ", Level.INFO, Logger.getLogger("org.hyperledger.fabric.shim").getLevel());
        assertEquals("Wrong log level for " + cb.getClass().getPackage().getName(), Level.INFO,
                Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());

        setLogLevelForChaincode(environmentVariables, cb, "WRONG", "WRONG");
        assertEquals("Wrong log level for org.hyperledger.fabric.shim ", Level.INFO, Logger.getLogger("org.hyperledger.fabric.shim").getLevel());
        assertEquals("Wrong log level for " + cb.getClass().getPackage().getName(), Level.INFO,
                Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());

        setLogLevelForChaincode(environmentVariables, cb, "DEBUG", "NOTICE");
        assertEquals("Wrong log level for org.hyperledger.fabric.shim ", Level.FINEST, Logger.getLogger("org.hyperledger.fabric.shim").getLevel());
        assertEquals("Wrong log level for " + cb.getClass().getPackage().getName(), Level.CONFIG,
                Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());

        setLogLevelForChaincode(environmentVariables, cb, "INFO", "WARNING");
        assertEquals("Wrong log level for org.hyperledger.fabric.shim ", Level.INFO, Logger.getLogger("org.hyperledger.fabric.shim").getLevel());
        assertEquals("Wrong log level for " + cb.getClass().getPackage().getName(), Level.WARNING,
                Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());

        setLogLevelForChaincode(environmentVariables, cb, "CRITICAL", "ERROR");
        assertEquals("Wrong log level for org.hyperledger.fabric.shim ", Level.SEVERE, Logger.getLogger("org.hyperledger.fabric.shim").getLevel());
        assertEquals("Wrong log level for " + cb.getClass().getPackage().getName(), Level.SEVERE,
                Logger.getLogger(cb.getClass().getPackage().getName()).getLevel());
    }

    public static void setLogLevelForChaincode(final EnvironmentVariables environmentVariables, final ChaincodeBase cb, final String shimLevel,
            final String chaincodeLevel) {
        environmentVariables.set(ChaincodeBase.CORE_CHAINCODE_LOGGING_SHIM, shimLevel);
        environmentVariables.set(ChaincodeBase.CORE_CHAINCODE_LOGGING_LEVEL, chaincodeLevel);
        cb.processEnvironmentOptions();
        cb.initializeLogging();
    }
}
