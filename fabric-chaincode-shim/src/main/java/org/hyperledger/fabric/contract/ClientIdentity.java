/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.protos.msp.Identities.SerializedIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ClientIdentity represents information about the identity that submitted a
 * transaction. Chaincodes can use this class to obtain information about the
 * submitting identity including a unique ID, the MSP (Membership Service
 * Provider) ID, and attributes. Such information is useful in enforcing access
 * control by the chaincode.
 *
 */
public final class ClientIdentity {
    private static Logger logger = Logger.getLogger(ContractRouter.class.getName());

    private final String mspId;
    private final X509Certificate cert;
    private Map<String, String> attrs;
    private final String id;
    // special OID used by Fabric to save attributes in x.509 certificates
    private static final String FABRIC_CERT_ATTR_OID = "1.2.3.4.5.6.7.8.1";

    /**
     * Creates new ClientIdentity helper.
     *
     * @param stub
     * @throws CertificateException
     * @throws JSONException
     * @throws IOException
     */
    public ClientIdentity(final ChaincodeStub stub) throws CertificateException, JSONException, IOException {
        final byte[] signingId = stub.getCreator();

        // Create a Serialized Identity protobuf
        final SerializedIdentity si = SerializedIdentity.parseFrom(signingId);
        this.mspId = si.getMspid();

        final byte[] idBytes = si.getIdBytes().toByteArray();

        final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(idBytes));
        this.cert = cert;

        this.attrs = new HashMap<String, String>();
        // Get the extension where the identity attributes are stored
        final byte[] extensionValue = cert.getExtensionValue(FABRIC_CERT_ATTR_OID);
        if (extensionValue != null) {
            this.attrs = parseAttributes(extensionValue);
        }

        // Populate identity
        this.id = "x509::" + cert.getSubjectDN().getName() + "::" + cert.getIssuerDN().getName();
    }

    /**
     * getId returns the ID associated with the invoking identity. This ID is
     * guaranteed to be unique within the MSP.
     *
     * @return {String} A string in the format: "x509::{subject DN}::{issuer DN}"
     */
    public String getId() {
        return this.id;
    }

    /**
     * getMSPID returns the MSP ID of the invoking identity.
     *
     * @return {String}
     */
    public String getMSPID() {
        return this.mspId;
    }

    /**
     * parseAttributes returns a map of the attributes associated with an identity.
     *
     * @param extensionValue DER-encoded Octet string stored in the attributes
     *                       extension of the certificate, as a byte array
     * @return attrMap {Map<String, String>} a map of identity attributes as key
     *         value pair strings
     * @throws IOException
     */
    private Map<String, String> parseAttributes(final byte[] extensionValue) throws IOException {

        final Map<String, String> attrMap = new HashMap<String, String>();

        // Create ASN1InputStream from extensionValue
        try (ByteArrayInputStream inStream = new ByteArrayInputStream(extensionValue); ASN1InputStream asn1InputStream = new ASN1InputStream(inStream)) {

            // Read the DER object
            final ASN1Primitive derObject = asn1InputStream.readObject();
            if (derObject instanceof DEROctetString) {
                final DEROctetString derOctetString = (DEROctetString) derObject;

                // Create attributeString from octets and create JSON object
                final String attributeString = new String(derOctetString.getOctets(), UTF_8);
                final JSONObject extJSON = new JSONObject(attributeString);
                final JSONObject attrs = extJSON.getJSONObject("attrs");

                final Iterator<String> keys = attrs.keys();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    // Populate map with attributes and values
                    attrMap.put(key, attrs.getString(key));
                }
            }
        } catch (final JSONException error) {
            // creating a JSON object failed
            // decoded extensionValue is not a string containing JSON
            logger.error(() -> logger.formatError(error));
            // return empty map
        }
        return attrMap;
    }

    /**
     * getAttributeValue returns the value of the client's attribute named
     * `attrName`. If the invoking identity possesses the attribute, returns the
     * value of the attribute. If the invoking identity does not possess the
     * attribute, returns null.
     *
     * @param attrName Name of the attribute to retrieve the value from the
     *                 identity's credentials (such as x.509 certificate for
     *                 PKI-based MSPs).
     * @return {String | null} Value of the attribute or null if the invoking
     *         identity does not possess the attribute.
     */
    public String getAttributeValue(final String attrName) {
        if (this.attrs.containsKey(attrName)) {
            return this.attrs.get(attrName);
        } else {
            return null;
        }
    }

    /**
     * assertAttributeValue verifies that the invoking identity has the attribute
     * named `attrName` with a value of `attrValue`.
     *
     * @param attrName  Name of the attribute to retrieve the value from the
     *                  identity's credentials (such as x.509 certificate for
     *                  PKI-based MSPs)
     * @param attrValue Expected value of the attribute
     * @return {boolean} True if the invoking identity possesses the attribute and
     *         the attribute value matches the expected value. Otherwise, returns
     *         false.
     */
    public boolean assertAttributeValue(final String attrName, final String attrValue) {
        if (!this.attrs.containsKey(attrName)) {
            return false;
        } else {
            return attrValue.equals(this.attrs.get(attrName));
        }
    }

    /**
     * getX509Certificate returns the X509 certificate associated with the invoking
     * identity, or null if it was not identified by an X509 certificate, for
     * instance if the MSP is implemented with an alternative to PKI such as
     * [Identity Mixer](https://jira.hyperledger.org/browse/FAB-5673).
     *
     * @return {X509Certificate | null}
     */
    public X509Certificate getX509Certificate() {
        return this.cert;
    }
}
