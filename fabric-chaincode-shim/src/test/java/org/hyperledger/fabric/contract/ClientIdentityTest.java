/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.hyperledger.fabric.TestUtil;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ClientIdentityTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test client identity can be created using certificate without attributes
     */
    @Test
    public void clientIdentityWithoutAttributes() throws Exception {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        final ClientIdentity identity = new ClientIdentity(stub);
        assertEquals(identity.getMSPID(), "testMSPID");
        assertEquals(identity.getId(),
                "x509::CN=admin, OU=Fabric, O=Hyperledger, ST=North Carolina, C=US::CN=example.com,"
                        + " OU=WWW, O=Internet Widgets, L=San Francisco, ST=California, C=US");
        assertEquals(identity.getAttributeValue("attr1"), null);
        assertEquals(identity.getAttributeValue("val1"), null);
        assertEquals(identity.getX509Certificate().getSubjectX500Principal().toString(),
                "CN=admin, OU=Fabric, O=Hyperledger, ST=North Carolina, C=US");
        assertEquals(identity.getX509Certificate().getSerialNumber(), new BigInteger("689287698446788666856807436918134903862142510628"));
    }

    /**
     * Test client identity can be created using certificate with attributes
     */
    @Test
    public void clientIdentityWithAttributes() throws Exception {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        ((ChaincodeStubNaiveImpl) stub).setCertificate(TestUtil.CERT_WITH_ATTRS);
        final ClientIdentity identity = new ClientIdentity(stub);
        assertEquals(identity.getMSPID(), "testMSPID");
        assertEquals(identity.getId(), "x509::CN=MyTestUserWithAttrs::CN=fabric-ca-server");
        assertEquals(identity.getAttributeValue("attr1"), "val1");
        assertEquals(identity.getAttributeValue("val1"), null);
        assertEquals(identity.assertAttributeValue("attr1", "val1"), true);
        assertEquals(identity.assertAttributeValue("attr1", "val2"), false);
        assertEquals(identity.assertAttributeValue("attr2", "val1"), false);
        assertEquals(identity.getX509Certificate().getSubjectX500Principal().toString(), "CN=MyTestUserWithAttrs");
        assertEquals(identity.getX509Certificate().getSerialNumber(), new BigInteger("172910998202207082780622887076293058980152824437"));
    }

    /**
     * Test client identity can be created using certificate with multiple
     * attributes
     */
    @Test
    public void clientIdentityWithMultipleAttributes() throws Exception {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        ((ChaincodeStubNaiveImpl) stub).setCertificate(TestUtil.CERT_MULTIPLE_ATTRIBUTES);
        final ClientIdentity identity = new ClientIdentity(stub);
        assertEquals(identity.getMSPID(), "testMSPID");
        assertEquals(identity.getId(), "x509::CN=test, OU=client::CN=ca.org1.example.com, O=org1.example.com, L=San Francisco, ST=California, C=US");
        assertEquals(identity.getAttributeValue("hello"), "world");
        assertEquals(identity.getAttributeValue("foo"), "bar");
        assertEquals(identity.getAttributeValue("attr1"), "val1");
        assertEquals(identity.getAttributeValue("val1"), null);
        assertEquals(identity.assertAttributeValue("hello", "world"), true);
        assertEquals(identity.assertAttributeValue("attr1", "val2"), false);
        assertEquals(identity.assertAttributeValue("hello", "val1"), false);
        assertEquals(identity.getX509Certificate().getSubjectX500Principal().toString(), "CN=test, OU=client");
        assertEquals(identity.getX509Certificate().getSerialNumber(), new BigInteger("400549269877250942864348502164024974865235124098"));
    }

    /**
     * Test client identity can be created using certificate with long distinguished
     * name
     */
    @Test
    public void clientIdentityWithLongDNs() throws Exception {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        ((ChaincodeStubNaiveImpl) stub).setCertificate(TestUtil.CERT_WITH_DNS);
        final ClientIdentity identity = new ClientIdentity(stub);
        assertEquals(identity.getMSPID(), "testMSPID");
        assertEquals(identity.getId(),
                "x509::CN=User1@org2.example.com, L=San Francisco, ST=California,"
                + " C=US::CN=ca.org2.example.com, O=org2.example.com, L=San Francisco, ST=California, C=US");
        assertEquals(identity.getX509Certificate().getSubjectX500Principal().toString(), "CN=User1@org2.example.com, L=San Francisco, ST=California, C=US");
        assertEquals(identity.getX509Certificate().getSerialNumber(), new BigInteger("175217963267961225716341475631843075227"));
    }

    /**
     * Test client identity throws a ContractRuntimeException when creating a
     * serialized identity fails
     */
    @Test
    public void catchInvalidProtocolBufferException() {
        thrown.expect(ContractRuntimeException.class);
        thrown.expectMessage("Could not create new client identity");

        final ChaincodeStub stub = mock(ChaincodeStub.class);
        when(stub.getCreator()).thenReturn("somethingInvalid".getBytes());
        ContextFactory.getInstance().createContext(stub);

    }

    /**
     * Test client identity attributes are empty when using a certificate with dummy
     * attributes
     */
    @Test
    public void createClientIdentityWithDummyAttributesCert() throws Exception {
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        // Create a certificate with rubbish attributes
        final String certWithDummyAttrs = TestUtil.createCertWithIdentityAttributes("{gsdhrlxhvcilgwoueglfs,djhzxo;vjs.dcx }");
        ((ChaincodeStubNaiveImpl) stub).setCertificate(certWithDummyAttrs);
        final ClientIdentity identity = new ClientIdentity(stub);

        assertEquals(identity.getMSPID(), "testMSPID");
        assertEquals(identity.getAttributeValue("attr1"), null);
        assertEquals(identity.getAttributeValue("val1"), null);
        assertEquals(identity.getX509Certificate().getSubjectX500Principal().toString(), "CN=test, OU=client");
    }
}
