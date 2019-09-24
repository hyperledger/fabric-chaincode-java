/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.hamcrest.Matchers;
import org.hyperledger.fabric.shim.chaincode.EmptyChaincode;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import io.grpc.netty.NettyChannelBuilder;

public class ChaincodeBaseTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMapLevel() {
        ChaincodeBase cb = new EmptyChaincode();
        assertEquals("Error maps", Level.SEVERE, proxyMapLevel(cb, "ERROR"));
        assertEquals("Critical maps", Level.SEVERE, proxyMapLevel(cb, "critical"));
        assertEquals("Warn maps", Level.WARNING, proxyMapLevel(cb, "WARNING"));
        assertEquals("Info maps", Level.INFO, proxyMapLevel(cb, "INFO"));
        assertEquals("Config maps", Level.CONFIG, proxyMapLevel(cb, " notice"));
        assertEquals("Info maps", Level.INFO, proxyMapLevel(cb, " info"));
        assertEquals("Debug maps", Level.FINEST, proxyMapLevel(cb, "debug          "));
        assertEquals("Info maps", Level.INFO, proxyMapLevel(cb, "wibble          "));
    }

    public Object proxyMapLevel(Object obj, Object... args) {

        try {
            Method m = ChaincodeBase.class.getDeclaredMethod("mapLevel", String.class);
            m.setAccessible(true);
            return m.invoke(obj, args);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testNewSuccessResponseEmpty() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse();
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertNull("Response message in not null", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewSuccessResponseWithMessage() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse("Simple message");
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewSuccessResponseWithPayload() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils
                .newSuccessResponse("Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertNull("Response message in not null", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(),
                "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewSuccessResponseWithMessageAndPayload() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newSuccessResponse("Simple message",
                "Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(),
                "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseEmpty() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse();
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertNull("Response message in not null", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithMessage() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse("Simple message");
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertNull("Response payload in not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithPayload() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils
                .newErrorResponse("Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertNull("Response message in not null", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(),
                "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseWithMessageAndPayload() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils.newErrorResponse("Simple message",
                "Simple payload".getBytes(Charset.defaultCharset()));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message in not correct", "Simple message", response.getMessage());
        assertArrayEquals("Response payload in not null", response.getPayload(),
                "Simple payload".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testNewErrorResponseWithException() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils
                .newErrorResponse(new Exception("Simple exception"));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message is not correct", "Unexpected error", response.getMessage());
        assertNull("Response payload is not null", response.getPayload());
    }

    @Test
    public void testNewErrorResponseWithChaincodeException() {
        org.hyperledger.fabric.shim.Chaincode.Response response = ResponseUtils
                .newErrorResponse(new ChaincodeException("Chaincode exception"));
        assertEquals("Response status is incorrect", response.getStatus(),
                org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR);
        assertEquals("Response message is not correct", "Chaincode exception", response.getMessage());
        assertNull("Response payload is not null", response.getPayload());
    }

    @Test
    public void testOptions() throws Exception {
        ChaincodeBase cb = new EmptyChaincode();

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
        } catch (IllegalArgumentException e) {
            fail("Wrong arguments");
        }

        cb.processCommandLineOptions(new String[] { "-i", "mycc1", "--peerAddress", "localhost.org:7053" });
        assertEquals("CCId incorrect", cb.getId(), "mycc1");
        assertEquals("Host incorrect", cb.getHost(), "localhost.org");
        assertEquals("Port incorrect", cb.getPort(), 7053);

        try {
            cb.validateOptions();
        } catch (IllegalArgumentException e) {
            fail("Wrong arguments");
        }

        cb.processCommandLineOptions(new String[] { "-i", "mycc1", "--peerAddress", "localhost1.org.7054" });
        assertEquals("Host incorrect", cb.getHost(), "localhost.org");
        assertEquals("Port incorrect", cb.getPort(), 7053);
    }

    @Test
    public void testUnsetOptionId() {
        ChaincodeBase cb = new EmptyChaincode();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(Matchers.containsString("The chaincode id must be specified"));
        cb.validateOptions();
    }

    @Test
    public void testUnsetOptionClientCertPath() {
        ChaincodeBase cb = new EmptyChaincode();
        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        cb.processEnvironmentOptions();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(Matchers.containsString("Client key certificate chain"));
        cb.validateOptions();
    }

    @Test
    public void testUnsetOptionClientKeyPath() {
        ChaincodeBase cb = new EmptyChaincode();
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
        ChaincodeBase cb = new EmptyChaincode();

        environmentVariables.set("CORE_CHAINCODE_ID_NAME", "mycc");
        environmentVariables.set("CORE_PEER_ADDRESS", "localhost:7052");
        environmentVariables.set("CORE_PEER_TLS_ENABLED", "true");
        environmentVariables.set("CORE_PEER_TLS_ROOTCERT_FILE", "src/test/resources/ca.crt");
        environmentVariables.set("CORE_TLS_CLIENT_KEY_PATH", "src/test/resources/client.key.enc");
        environmentVariables.set("CORE_TLS_CLIENT_CERT_PATH", "src/test/resources/client.crt.enc");

        cb.processEnvironmentOptions();
        cb.validateOptions();
        assertTrue("Not correct builder", cb.newChannelBuilder() instanceof NettyChannelBuilder);
    }

    @Test
    public void testInitializeLogging() {
        ChaincodeBase cb = new EmptyChaincode();               
        cb.processEnvironmentOptions();
        cb.initializeLogging();

        assertTrue("Wrong log level for org.hyperledger.fabric.shim ",
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.INFO));                

        setLogLevelForChaincode(environmentVariables, cb,  "WRONG SO LOG AT INFO");
        assertTrue("Wrong log level for org.hyperledger.fabric.shim ", 
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.INFO));

        setLogLevelForChaincode(environmentVariables, cb,  "NOTICE");
        assertTrue("Wrong log level for org.hyperledger.fabric.shim ", 
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.CONFIG));

        setLogLevelForChaincode(environmentVariables, cb, "WARNING");
        assertTrue("Wrong log level for org.hyperledger.fabric.shim ", 
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.WARNING));

        setLogLevelForChaincode(environmentVariables, cb,  "ERROR");
        assertTrue("Wrong log level for org.hyperledger.fabric.shim ",
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.SEVERE));

        setLogLevelForChaincode(environmentVariables, cb,  "DEBUG");
        assertTrue("Wrong log level for org.hyperledger.fabric.shim ",
                Logger.getLogger("org.hyperledger.fabric.shim").isLoggable(Level.FINE));
    }

    public static void setLogLevelForChaincode(EnvironmentVariables environmentVariables, ChaincodeBase cb,
            String chaincodeLelev) {
        
        environmentVariables.set(ChaincodeBase.CORE_CHAINCODE_LOGGING_LEVEL, chaincodeLelev);
        cb.processEnvironmentOptions();
        cb.initializeLogging();
    }
}
