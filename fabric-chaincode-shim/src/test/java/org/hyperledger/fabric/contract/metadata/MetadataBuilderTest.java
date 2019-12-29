/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.metadata;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.hyperledger.fabric.contract.ChaincodeStubNaiveImpl;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.impl.ContractDefinitionImpl;
import org.hyperledger.fabric.contract.systemcontract.SystemContract;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import contract.SampleContract;

public class MetadataBuilderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String expectedMetadataString = "    {\n" + "       \"components\": {\"schemas\": {}},\n"
            + "       \"$schema\": \"https://fabric-shim.github.io/contract-schema.json\",\n" + "       \"contracts\": {\"SampleContract\": {\n"
            + "          \"name\": \"SampleContract\",\n" + "          \"transactions\": [],\n" + "          \"info\": {\n"
            + "             \"license\": {\"name\": \"\"},\n" + "             \"description\": \"\",\n" + "             \"termsOfService\": \"\",\n"
            + "             \"title\": \"\",\n" + "             \"version\": \"\",\n" + "             \"contact\": {\"email\": \"fred@example.com\"}\n"
            + "          }\n" + "       }},\n" + "       \"info\": {\n" + "          \"license\": {\"name\": \"\"},\n" + "          \"description\": \"\",\n"
            + "          \"termsOfService\": \"\",\n" + "          \"title\": \"\",\n" + "          \"version\": \"\",\n"
            + "          \"contact\": {\"email\": \"fred@example.com\"}\n" + "       }\n" + "    }\n" + "";

    // fields are private, so use reflection to bypass this for unit testing
    private void setMetadataBuilderField(final String name, final Object value) {
        try {
            final Field f = MetadataBuilder.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(null, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to set field " + e.getMessage());
        }
    }

    @Before
    @After
    public void beforeAndAfterEach() {

        setMetadataBuilderField("componentMap", new HashMap<String, Object>());
        setMetadataBuilderField("contractMap", new HashMap<String, HashMap<String, Serializable>>());
        setMetadataBuilderField("overallInfoMap", new HashMap<String, Object>());
        setMetadataBuilderField("schemaClient", new DefaultSchemaClient());

    }

    @Test
    public void systemContract() {

        final SystemContract system = new SystemContract();
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        system.getMetadata(new Context(stub));
    }

    @Test
    public void defaultSchemasNotLoadedFromNetwork() {
        final ContractDefinition contractDefinition = new ContractDefinitionImpl(SampleContract.class);
        MetadataBuilder.addContract(contractDefinition);
        setMetadataBuilderField("schemaClient", new SchemaClient() {

            @Override
            public InputStream get(final String uri) {
                throw new RuntimeException("Refusing to load schema: " + uri);
            }

        });
        MetadataBuilder.validate();
    }

}
