/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.metadata;

import java.io.InputStream;
import java.io.Serializable;
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

    String expectedMetadataString = "    {\n" + "       \"components\": {\"schemas\": {}},\n"
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
    @After
    public void beforeAndAfterEach() {
        MetadataBuilder.componentMap = new HashMap<String, Object>();
        MetadataBuilder.contractMap = new HashMap<String, HashMap<String, Serializable>>();
        MetadataBuilder.overallInfoMap = new HashMap<String, Object>();
        MetadataBuilder.schemaClient = new DefaultSchemaClient();
    }

    @Test
    public void systemContract() {

        SystemContract system = new SystemContract();
        ChaincodeStub stub = new ChaincodeStubNaiveImpl();
        // TODO: Assert something about the returned metadata
        system.getMetadata(new Context(stub));
    }

    @Test
    public void defaultSchemasNotLoadedFromNetwork() {
        ContractDefinition contractDefinition = new ContractDefinitionImpl(SampleContract.class);
        MetadataBuilder.addContract(contractDefinition);
        MetadataBuilder.schemaClient = new SchemaClient(){

            @Override
            public InputStream get(String uri) {
                throw new RuntimeException("Refusing to load schema: " + uri);
            }

        };
        MetadataBuilder.validate();
    }

}