/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.erosb.jsonsKema.JsonParser;
import com.github.erosb.jsonsKema.JsonValue;
import com.github.erosb.jsonsKema.Schema;
import com.github.erosb.jsonsKema.SchemaClient;
import com.github.erosb.jsonsKema.SchemaLoader;
import com.github.erosb.jsonsKema.SchemaLoaderConfig;
import com.github.erosb.jsonsKema.ValidationFailure;
import com.github.erosb.jsonsKema.Validator;
import static com.github.erosb.jsonsKema.SchemaLoaderConfig.createDefaultConfig;

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
    private static SchemaClient schemaClient = null;

    /**
     * Validation method.
     *
     * @throws Exception if the metadata is not valid
     */
    public static void validate() {
        logger.info("Running schema test validation");
        final ClassLoader cl = MetadataBuilder.class.getClassLoader();
        try (InputStream contractSchemaInputStream = cl.getResourceAsStream("contract-schema.json");
            InputStream jsonSchemaInputStream = cl.getResourceAsStream("json-schema-draft-04-schema.json")) {

            // Base level schema for JSON itself pulled from class loader into
            // String into JsonValue
            final String jsonSchemaRaw = loadInputStream(jsonSchemaInputStream);
            logger.debug("Loaded base level JSON schema: " + jsonSchemaRaw);

            // Now we can do a configuration of that, linking that schema to
            // that URL, which is the default URL, for the record.

            URI jsonSchemaLocation = null;

            try {
                jsonSchemaLocation = new URI("http://json-schema.org/draft-04/schema");
            } catch (URISyntaxException use) {
                logger.error(use.toString());
            }

            SchemaLoaderConfig config = createDefaultConfig(Map.of(
                jsonSchemaLocation,
                jsonSchemaRaw
            ));

            // Now we load in the Contract Schema from the class loader.
            // It then goes immediately into a JsonValue.
            final String contractSchemaRaw = loadInputStream(contractSchemaInputStream);
            logger.debug("Loaded contract schema: " + contractSchemaRaw);
            final JsonValue contractSchemaParsed = new JsonParser(contractSchemaRaw).parse();

            // Putting them together, we can say that we have a SchemaLoader that
            // uses the JSON schema to load in the contract schema.
            final Schema schema = new SchemaLoader(contractSchemaParsed, config).load();

            // Now we need to grab the current metadata as a raw String also
            final String currentMetadata = getMetadata();
            // And pass that into a JsonValue as well.
            final JsonValue parsedCurrentMetadata = new JsonParser(currentMetadata).parse();

            // Now we can validate the parsed current metadata using the contract
            // schema, which is in turn referring to the JSON schema.
            final ValidationFailure failures = Validator.forSchema(schema).validate(parsedCurrentMetadata);

            // Note that this used to throw an exception when validation failed, and it no longer does.
            if (null != failures) {
                logger.error("Failures occurred when validating the contract schema: " + failures);
                throw new RuntimeException("Failures encountered when validating the contract schema!");
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
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
        if (txFunction.getType() == TransactionType.SUBMIT) { // add deprecated tags
            tags.add(TransactionType.INVOKE);
        } else {
            tags.add(TransactionType.QUERY);
        }

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
     * Using the JDK/JRE only, load all the data from an InputStream into a String.
     * 
     * @param inputStreamToLoad The input stream we'd like to load in
     * @return String of the total contents of the input stream
     */
    private static String loadInputStream(final InputStream inputStreamToLoad) {
        final Scanner scanner = new Scanner(inputStreamToLoad).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
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
