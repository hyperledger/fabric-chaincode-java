/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public final class TestUtil {

    private TestUtil() {

    }

    public static final String CERT_WITHOUT_ATTRS = "MIICXTCCAgSgAwIBAgIUeLy6uQnq8wwyElU/jCKRYz3tJiQwCgYIKoZIzj0EAwIw"
            + "eTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh" + "biBGcmFuY2lzY28xGTAXBgNVBAoTEEludGVybmV0IFdpZGdldHMxDDAKBgNVBAsT"
            + "A1dXVzEUMBIGA1UEAxMLZXhhbXBsZS5jb20wHhcNMTcwOTA4MDAxNTAwWhcNMTgw" + "OTA4MDAxNTAwWjBdMQswCQYDVQQGEwJVUzEXMBUGA1UECBMOTm9ydGggQ2Fyb2xp"
            + "bmExFDASBgNVBAoTC0h5cGVybGVkZ2VyMQ8wDQYDVQQLEwZGYWJyaWMxDjAMBgNV" + "BAMTBWFkbWluMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEFq/90YMuH4tWugHa"
            + "oyZtt4Mbwgv6CkBSDfYulVO1CVInw1i/k16DocQ/KSDTeTfgJxrX1Ree1tjpaodG" + "1wWyM6OBhTCBgjAOBgNVHQ8BAf8EBAMCB4AwDAYDVR0TAQH/BAIwADAdBgNVHQ4E"
            + "FgQUhKs/VJ9IWJd+wer6sgsgtZmxZNwwHwYDVR0jBBgwFoAUIUd4i/sLTwYWvpVr" + "TApzcT8zv/kwIgYDVR0RBBswGYIXQW5pbHMtTWFjQm9vay1Qcm8ubG9jYWwwCgYI"
            + "KoZIzj0EAwIDRwAwRAIgCoXaCdU8ZiRKkai0QiXJM/GL5fysLnmG2oZ6XOIdwtsC" + "IEmCsI8Mhrvx1doTbEOm7kmIrhQwUVDBNXCWX1t3kJVN";

    public static final String CERT_WITH_ATTRS = "MIIB6TCCAY+gAwIBAgIUHkmY6fRP0ANTvzaBwKCkMZZPUnUwCgYIKoZIzj0EAwIw"
            + "GzEZMBcGA1UEAxMQZmFicmljLWNhLXNlcnZlcjAeFw0xNzA5MDgwMzQyMDBaFw0x" + "ODA5MDgwMzQyMDBaMB4xHDAaBgNVBAMTE015VGVzdFVzZXJXaXRoQXR0cnMwWTAT"
            + "BgcqhkjOPQIBBggqhkjOPQMBBwNCAATmB1r3CdWvOOP3opB3DjJnW3CnN8q1ydiR" + "dzmuA6A2rXKzPIltHvYbbSqISZJubsy8gVL6GYgYXNdu69RzzFF5o4GtMIGqMA4G"
            + "A1UdDwEB/wQEAwICBDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTYKLTAvJJK08OM" + "VGwIhjMQpo2DrjAfBgNVHSMEGDAWgBTEs/52DeLePPx1+65VhgTwu3/2ATAiBgNV"
            + "HREEGzAZghdBbmlscy1NYWNCb29rLVByby5sb2NhbDAmBggqAwQFBgcIAQQaeyJh" + "dHRycyI6eyJhdHRyMSI6InZhbDEifX0wCgYIKoZIzj0EAwIDSAAwRQIhAPuEqWUp"
            + "svTTvBqLR5JeQSctJuz3zaqGRqSs2iW+QB3FAiAIP0mGWKcgSGRMMBvaqaLytBYo" + "9v3hRt1r8j8vN0pMcg==";

    public static final String CERT_WITH_DNS = "MIICGjCCAcCgAwIBAgIRAIPRwJHVLhHK47XK0BbFZJswCgYIKoZIzj0EAwIwczEL"
            + "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG" + "cmFuY2lzY28xGTAXBgNVBAoTEG9yZzIuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh"
            + "Lm9yZzIuZXhhbXBsZS5jb20wHhcNMTcwNjIzMTIzMzE5WhcNMjcwNjIxMTIzMzE5" + "WjBbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN"
            + "U2FuIEZyYW5jaXNjbzEfMB0GA1UEAwwWVXNlcjFAb3JnMi5leGFtcGxlLmNvbTBZ" + "MBMGByqGSM49AgEGCCqGSM49AwEHA0IABBd9SsEiFH1/JIb3qMEPLR2dygokFVKW"
            + "eINcB0Ni4TBRkfIWWUJeCANTUY11Pm/+5gs+fBTqBz8M2UzpJDVX7+2jTTBLMA4G" + "A1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1UdIwQkMCKAIKfUfvpGproH"
            + "cwyFD+0sE3XfJzYNcif0jNwvgOUFZ4AFMAoGCCqGSM49BAMCA0gAMEUCIQC8NIMw" + "e4ym/QRwCJb5umbONNLSVQuEpnPsJrM/ssBPvgIgQpe2oYa3yO3USro9nBHjpM3L"
            + "KsFQrpVnF8O6hoHOYZQ=";

    public static final String CERT_MULTIPLE_ATTRIBUTES = "MIIChzCCAi6gAwIBAgIURilAHeqwLu/fNUv8eZoGPRh3H4IwCgYIKoZIzj0EAwIw"
            + "czELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh" + "biBGcmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMT"
            + "E2NhLm9yZzEuZXhhbXBsZS5jb20wHhcNMTkwNzMxMTYxNzAwWhcNMjAwNzMwMTYy" + "MjAwWjAgMQ8wDQYDVQQLEwZjbGllbnQxDTALBgNVBAMTBHRlc3QwWTATBgcqhkjO"
            + "PQIBBggqhkjOPQMBBwNCAAR2taQK8w7D3hr3gBxCz+8eV4KSv7pFQfNjDHMMe9J9" + "LJwcLpVTT5hYiLLRaqQonLBxBE3Ey0FneySvFuBScas3o4HyMIHvMA4GA1UdDwEB"
            + "/wQEAwIHgDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQi3mhXS/WzcjBniwAmPdYP" + "kHqVVzArBgNVHSMEJDAigCC7VXjmSEugjAB/A0S6vfMxLsUIgag9WVNwtwwebnRC"
            + "7TCBggYIKgMEBQYHCAEEdnsiYXR0cnMiOnsiYXR0cjEiOiJ2YWwxIiwiZm9vIjoi" + "YmFyIiwiaGVsbG8iOiJ3b3JsZCIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5y"
            + "b2xsbWVudElEIjoidGVzdCIsImhmLlR5cGUiOiJjbGllbnQifX0wCgYIKoZIzj0E" + "AwIDRwAwRAIgQxEFvnZTEsf3CSZmp9IYsxcnEOtVYleOd86LAKtk1wICIH7XOPwW"
            + "/RE4Z8WLZzFei/78Oezbx6obOvBxPMsVWRe5";

    /**
     * Function to create a certificate with dummy attributes
     *
     * @param attributeValue {String} value to be written to the identity attributes
     *                       section of the certificate
     * @return encodedCert {String} encoded certificate with re-written attributes
     */
    public static String createCertWithIdentityAttributes(final String attributeValue) throws Exception {

        // Use existing certificate with attributes
        final byte[] decodedCert = Base64.getDecoder().decode(CERT_MULTIPLE_ATTRIBUTES);
        // Create a certificate holder and builder
        final X509CertificateHolder certHolder = new X509CertificateHolder(decodedCert);
        final X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(certHolder);

        // special OID used by Fabric to save attributes in x.509 certificates
        final String fabricCertOid = "1.2.3.4.5.6.7.8.1";
        // Write the new attribute value
        final byte[] extDataToWrite = attributeValue.getBytes();
        certBuilder.replaceExtension(new ASN1ObjectIdentifier(fabricCertOid), true, extDataToWrite);

        // Create a privateKey
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(384);
        final KeyPair keyPair = generator.generateKeyPair();

        // Create and build the Content Signer
        final JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256withECDSA");
        final ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getPrivate());
        // Build the Certificate from the certificate builder
        final X509CertificateHolder builtCert = certBuilder.build(contentSigner);
        final X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(builtCert.getEncoded()));
        final String encodedCert = Base64.getEncoder().encodeToString(certificate.getEncoded());
        return encodedCert;
    }
}
