/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.metadata;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
    public void propertySchema() {

        Map<String, Object> retval = MetadataBuilder.propertySchema(String.class);
        assertThat(retval, hasEntry("type", "string"));

        retval = MetadataBuilder.propertySchema(byte.class);
        assertThat(retval, hasEntry("type", "integer"));
        assertThat(retval, hasEntry("format", "int8"));

        retval = MetadataBuilder.propertySchema(short.class);
        assertThat(retval, hasEntry("type", "integer"));
        assertThat(retval, hasEntry("format", "int16"));

        retval = MetadataBuilder.propertySchema(int.class);
        assertThat(retval, hasEntry("type", "integer"));
        assertThat(retval, hasEntry("format", "int32"));

        retval = MetadataBuilder.propertySchema(long.class);
        assertThat(retval, hasEntry("type", "integer"));
        assertThat(retval, hasEntry("format", "int64"));

        retval = MetadataBuilder.propertySchema(double.class);
        assertThat(retval, hasEntry("type", "number"));
        assertThat(retval, hasEntry("format", "double"));

        retval = MetadataBuilder.propertySchema(float.class);
        assertThat(retval, hasEntry("type", "number"));
        assertThat(retval, hasEntry("format", "float"));

        retval = MetadataBuilder.propertySchema(boolean.class);
        assertThat(retval, hasEntry("type", "boolean"));

        retval = MetadataBuilder.propertySchema(Exception.class);
        assertNull(retval);
    }

    @Test
    public void systemContract() {

        // access the system contract to extract the metadata
        SystemContract system = new SystemContract();
        String metadatacompressed = system.getMetadata();
        System.out.println(metadatacompressed);
    }

}