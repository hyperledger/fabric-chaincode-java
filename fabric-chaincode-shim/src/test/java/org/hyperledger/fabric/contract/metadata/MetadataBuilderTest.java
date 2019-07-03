/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.metadata;

import java.io.Serializable;
import java.util.HashMap;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.systemcontract.SystemContract;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MetadataBuilderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    String expectedJSON = "    {\n" + "       \"components\": {\"schemas\": {}},\n"
            + "       \"$schema\": \"https://fabric-shim.github.io/contract-schema.json\",\n"
            + "       \"contracts\": {\"SampleContract\": {\n" + "          \"name\": \"SampleContract\",\n"
            + "          \"transactions\": [],\n" + "          \"info\": {\n"
            + "             \"license\": {\"name\": \"\"},\n" + "             \"description\": \"\",\n"
            + "             \"termsOfService\": \"\",\n" + "             \"title\": \"\",\n"
            + "             \"version\": \"\",\n" + "             \"contact\": {\"email\": \"fred@example.com\"}\n"
            + "          }\n" + "       }},\n" + "       \"info\": {\n" + "          \"license\": {\"name\": \"\"},\n"
            + "          \"description\": \"\",\n" + "          \"termsOfService\": \"\",\n"
            + "          \"title\": \"\",\n" + "          \"version\": \"\",\n"
            + "          \"contact\": {\"email\": \"fred@example.com\"}\n" + "       }\n" + "    }\n" + "";

    @Before
    public void beforeEach() {
        MetadataBuilder.componentMap = new HashMap<String, Object>();
        MetadataBuilder.contractMap = new HashMap<String, HashMap<String, Serializable>>();
        MetadataBuilder.overallInfoMap = new HashMap<String, Object>();
    }

    @Test
    public void systemContract() {

        // access the system contract to extract the metadata
        SystemContract system = new SystemContract();
        String metadatacompressed = system.getMetadata(new Context(null));

    }

}