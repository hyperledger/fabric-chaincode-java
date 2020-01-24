/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.everit.json.schema.loader.internal.DefaultSchemaClient;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TransactionType;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Builder to assist in production of the metadata.
 * <p>
 * This class is used to build up the JSON structure to be returned as the
 * metadata It is not a store of information, rather a set of functional data to
 * process to and from metadata json to the internal data structure
 */
public final class MetadataBuilder {
    private static Logger logger = Logger.getLogger(MetadataBuilder.class);

    private MetadataBuilder() {

    }

    @SuppressWarnings("serial")
    static class MetadataMap<K, V> extends HashMap<K, V> {

        V putIfNotNull(final K key, final V value) {
            logger.info(key + " " + value);
            if (value != null && !value.toString().isEmpty()) {
                return put(key, value);
            } else {
                return null;
            }
        }
    }

    // Metadata is composed of three primary sections
    // each of which is stored in a map
    private static Map<String, HashMap<String, Serializable>> contractMap = new HashMap<>();
    private static Map<String, Object> overallInfoMap = new HashMap<String, Object>();
    private static Map<String, Object> componentMap = new HashMap<String, Object>();

    // The schema client used to load any other referenced schemas
    private static SchemaClient schemaClient = new DefaultSchemaClient();

    /**
     * Validation method.
     *
     * @throws ValidationException if the metadata is not valid
     */
    public static void validate() {
        logger.info("Running schema test validation");
        final ClassLoader cl = MetadataBuilder.class.getClassLoader();
        try (InputStream contractSchemaInputStream = cl.getResourceAsStream("contract-schema.json");
                InputStream jsonSchemaInputStream = cl.getResourceAsStream("json-schema-draft-04-schema.json")) {
            final JSONObject rawContractSchema = new JSONObject(new JSONTokener(contractSchemaInputStream));
            final JSONObject rawJsonSchema = new JSONObject(new JSONTokener(jsonSchemaInputStream));
            final SchemaLoader schemaLoader = SchemaLoader.builder().schemaClient(schemaClient).schemaJson(rawContractSchema)
                    .registerSchemaByURI(URI.create("http://json-schema.org/draft-04/schema"), rawJsonSchema).build();
            final Schema schema = schemaLoader.load().build();
            schema.validate(metadata());

        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final ValidationException e) {
            logger.error(e.getMessage());
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(logger::info);
            logger.error(debugString());
            throw e;
        }

    }

    /**
     * Setup the metadata from the found contracts.
     *
     * @param registry     RoutingRegistry
     * @param typeRegistry TypeRegistry
     */
    public static void initialize(final RoutingRegistry registry, final TypeRegistry typeRegistry) {
        final Collection<ContractDefinition> contractDefinitions = registry.getAllDefinitions();
        contractDefinitions.forEach(MetadataBuilder::addContract);

        final Collection<DataTypeDefinition> dataTypes = typeRegistry.getAllDataTypes();
        dataTypes.forEach(MetadataBuilder::addComponent);

        // need to validate that the metadata that has been created is really valid
        // it should be as it's been created by code, but this is a valuable double
        // check
        logger.info("Validating schema created");
        MetadataBuilder.validate();

    }

    /**
     * Adds a component/ complex data-type.
     *
     * @param datatype DataTypeDefinition
     */
    public static void addComponent(final DataTypeDefinition datatype) {

        final Map<String, Object> component = new HashMap<>();

        component.put("$id", datatype.getName());
        component.put("type", "object");
        component.put("additionalProperties", false);

        final Map<String, TypeSchema> propertiesMap = datatype.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> (e.getValue().getSchema())));
        component.put("properties", propertiesMap);

        componentMap.put(datatype.getSimpleName(), component);
    }

    /**
     * Adds a new contract to the metadata as represented by the class object.
     *
     * @param contractDefinition Class of the object to use as a contract
     * @return the key that the contract class is referred to in the metadata
     */
    @SuppressWarnings("serial")
    public static String addContract(final ContractDefinition contractDefinition) {

        final String key = contractDefinition.getName();

        final Contract annotation = contractDefinition.getAnnotation();

        final Info info = annotation.info();
        final HashMap<String, Object> infoMap = new HashMap<String, Object>();
        infoMap.put("title", info.title());
        infoMap.put("description", info.description());
        infoMap.put("termsOfService", info.termsOfService());
        infoMap.put("contact", new MetadataMap<String, String>() {
            {
                putIfNotNull("email", info.contact().email());
                putIfNotNull("name", info.contact().name());
                putIfNotNull("url", info.contact().url());
            }
        });
        infoMap.put("license", new MetadataMap<String, String>() {
            {
                put("name", info.license().name());
                putIfNotNull("url", info.license().url());
            }
        });
        infoMap.put("version", info.version());

        final HashMap<String, Serializable> contract = new HashMap<String, Serializable>();
        contract.put("name", key);
        contract.put("transactions", new ArrayList<Object>());
        contract.put("info", infoMap);

        contractMap.put(key, contract);
        final boolean defaultContract = true;
        if (defaultContract) {
            overallInfoMap.putAll(infoMap);
        }

        final Collection<TxFunction> fns = contractDefinition.getTxFunctions();
        fns.forEach(txFn -> {
            MetadataBuilder.addTransaction(txFn, key);
        });

        return key;
    }

    /**
     * Adds a new transaction function to the metadata for the given contract.
     *
     * @param txFunction   Object representing the transaction function
     * @param contractName Name of the contract that this function belongs to
     */
    public static void addTransaction(final TxFunction txFunction, final String contractName) {
        final TypeSchema transaction = new TypeSchema();
        final TypeSchema returnSchema = txFunction.getReturnSchema();
        if (returnSchema != null) {
            transaction.put("returns", returnSchema);
        }

        final ArrayList<TransactionType> tags = new ArrayList<TransactionType>();
        tags.add(txFunction.getType());

        final Map<String, Serializable> contract = contractMap.get(contractName);
        @SuppressWarnings("unchecked")
        final List<Object> txs = (ArrayList<Object>) contract.get("transactions");

        final ArrayList<TypeSchema> paramsList = new ArrayList<TypeSchema>();
        txFunction.getParamsList().forEach(pd -> {
            final TypeSchema paramMap = pd.getSchema();
            paramMap.put("name", pd.getName());
            paramsList.add(paramMap);
        });

        transaction.put("parameters", paramsList);

        if (tags.size() != 0) {
            transaction.put("tags", tags.toArray());
            transaction.put("name", txFunction.getName());
            txs.add(transaction);
        }
    }

    /**
     * Returns the metadata as a JSON string (compact).
     *
     * @return metadata as String
     */
    public static String getMetadata() {
        return metadata().toString();
    }

    private static final int PADDING = 3;

    /**
     * Returns the metadata as a JSON string (spaced out for humans).
     *
     * @return metadata as a spaced out string for humans
     */
    public static String debugString() {
        return metadata().toString(PADDING);
    }

    /**
     * Create a JSONObject representing the schema.
     *
     * @return JSONObject of the metadata
     */
    private static JSONObject metadata() {
        final HashMap<String, Object> metadata = new HashMap<String, Object>();

        metadata.put("$schema", "https://fabric-shim.github.io/release-1.4/contract-schema.json");
        metadata.put("info", overallInfoMap);
        metadata.put("contracts", contractMap);
        metadata.put("components", Collections.singletonMap("schemas", componentMap));

        final JSONObject joMetadata = new JSONObject(metadata);
        return joMetadata;
    }

    /**
     *
     * @return All the components indexed by name
     */
    public static Map<?, ?> getComponents() {
        return componentMap;
    }
}
