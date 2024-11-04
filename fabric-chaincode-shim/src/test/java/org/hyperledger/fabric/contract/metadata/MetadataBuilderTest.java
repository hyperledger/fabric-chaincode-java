/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.metadata;

import contract.SampleContract;
import java.io.InputStream;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class MetadataBuilderTest {
    // fields are private, so use reflection to bypass this for unit testing
    private void setMetadataBuilderField(final String name, final Object value)
            throws NoSuchFieldException, IllegalAccessException {
        final Field f = MetadataBuilder.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    @BeforeEach
    @AfterEach
    void beforeAndAfterEach() throws NoSuchFieldException, IllegalAccessException {

        setMetadataBuilderField("componentMap", new HashMap<>());
        setMetadataBuilderField("contractMap", new HashMap<>());
        setMetadataBuilderField("overallInfoMap", new HashMap<>());
        setMetadataBuilderField("schemaClient", new DefaultSchemaClient());
    }

    @Test
    void systemContract() {

        final SystemContract system = new SystemContract();
        final ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        system.getMetadata(new Context(stub));
    }

    @Test
    void defaultSchemasNotLoadedFromNetwork() throws NoSuchFieldException, IllegalAccessException {
        final ContractDefinition contractDefinition = new ContractDefinitionImpl(SampleContract.class);
        MetadataBuilder.addContract(contractDefinition);
        setMetadataBuilderField("schemaClient", new SchemaClient() {

            @Override
            public InputStream get(final String uri) {
                throw new IllegalStateException("Refusing to load schema: " + uri);
            }
        });
        MetadataBuilder.validate();
    }
}
