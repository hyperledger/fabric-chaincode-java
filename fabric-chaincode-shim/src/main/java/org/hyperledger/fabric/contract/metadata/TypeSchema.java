/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.metadata;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONObject;

/**
 * TypeSchema.
 *
 * <p>Custom sub-type of Map that helps with the case where if there's no value then do not insert the property at all
 *
 * <p>Does not include the "schema" top level map
 */
@SuppressWarnings("PMD.GodClass")
public final class TypeSchema extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TypeSchema.class.getName());

    private static final String SCHEMA_PROP = "schema";
    private static final String TYPE_PROP = "type";
    private static final String ITEMS_PROP = "items";
    private static final String FORMAT_PROP = "format";
    private static final String INTEGER_TYPE = "integer";

    private Object putInternal(final String key, final Object value) {
        if (value != null && !value.toString().isEmpty()) {
            return put(key, value);
        } else {
            return null;
        }
    }

    String putIfNotNull(final String key, final String value) {
        return (String) this.putInternal(key, value);
    }

    String[] putIfNotNull(final String key, final String[] value) {
        return (String[]) this.putInternal(key, value);
    }

    TypeSchema putIfNotNull(final String key, final TypeSchema value) {
        return (TypeSchema) this.putInternal(key, value);
    }

    TypeSchema[] putIfNotNull(final String key, final TypeSchema[] value) {
        return (TypeSchema[]) this.putInternal(key, value);
    }

    /** @return Return Type String */
    public String getType() {
        if (this.containsKey(SCHEMA_PROP)) {
            final Map<?, ?> intermediateMap = (Map<?, ?>) this.get(SCHEMA_PROP);
            return (String) intermediateMap.get(TYPE_PROP);
        }
        return (String) this.get(TYPE_PROP);
    }

    /** @return TypeSchema items */
    public TypeSchema getItems() {
        if (this.containsKey(SCHEMA_PROP)) {
            final Map<?, ?> intermediateMap = (Map<?, ?>) this.get(SCHEMA_PROP);
            return (TypeSchema) intermediateMap.get(ITEMS_PROP);
        }
        return (TypeSchema) this.get(ITEMS_PROP);
    }

    /** @return Reference */
    public String getRef() {
        if (this.containsKey(SCHEMA_PROP)) {
            final Map<?, ?> intermediateMap = (Map<?, ?>) this.get(SCHEMA_PROP);
            return (String) intermediateMap.get("$ref");
        }
        return (String) this.get("$ref");
    }

    /** @return Format */
    public String getFormat() {
        if (this.containsKey(SCHEMA_PROP)) {
            final Map<?, ?> intermediateMap = (Map<?, ?>) this.get(SCHEMA_PROP);
            return (String) intermediateMap.get(FORMAT_PROP);
        }
        return (String) this.get(FORMAT_PROP);
    }

    /**
     * @param typeRegistry
     * @return Class object
     */
    public Class<?> getTypeClass(final TypeRegistry typeRegistry) {
        String type = Optional.ofNullable(getType()).orElse("object");

        switch (type) {
            case "object":
                return getObjectClass(typeRegistry);
            case "string":
                return getStringClass();
            case INTEGER_TYPE:
                return getIntegerClass();
            case "number":
                return getNumberClass();
            case "boolean":
                return boolean.class;
            case "array":
                return getArrayClass(typeRegistry);
            default:
                return null;
        }
    }

    private Class<?> getArrayClass(final TypeRegistry typeRegistry) {
        final TypeSchema typdef = this.getItems();
        final Class<?> arrayType = typdef.getTypeClass(typeRegistry);
        return Array.newInstance(arrayType, 0).getClass();
    }

    private Class<?> getNumberClass() {
        switch (getFormat()) {
            case "double":
                return double.class;
            case "float":
                return float.class;
            default:
                throw new IllegalArgumentException("Unknown format for number of " + getFormat());
        }
    }

    private Class<?> getIntegerClass() {
        // need to check the format
        switch (getFormat()) {
            case "int8":
                return byte.class;
            case "int16":
                return short.class;
            case "int32":
                return int.class;
            case "int64":
                return long.class;
            default:
                throw new IllegalArgumentException("Unknown format for integer of " + getFormat());
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private Class<?> getStringClass() {
        if ("uint16".equals(getFormat())) {
            return char.class;
        }
        return String.class;
    }

    private Class<?> getObjectClass(final TypeRegistry typeRegistry) {
        final String ref = this.getRef();
        final String format = ref.substring(ref.lastIndexOf('/') + 1);
        return typeRegistry.getDataType(format).getTypeClass();
    }

    /**
     * Provide a mapping between the Java Language types and the OpenAPI based types.
     *
     * @param clz
     * @return TypeSchema
     */
    @SuppressWarnings({"PMD.ReturnEmptyCollectionRatherThanNull", "PMD.AvoidLiteralsInIfCondition"})
    public static TypeSchema typeConvert(final Class<?> clz) {
        String className = clz.getTypeName();

        if ("void".equals(className)) {
            return null;
        }

        final TypeSchema result = new TypeSchema();
        TypeSchema schema = result;

        if (clz.isArray()) {
            result.put(TYPE_PROP, "array");

            schema = new TypeSchema();
            final Class<?> componentClass = clz.getComponentType();
            className = componentClass.getTypeName();

            // double check the componentType
            if (componentClass.isArray()) {
                // nested arrays
                result.put(ITEMS_PROP, typeConvert(componentClass));
            } else {
                result.put(ITEMS_PROP, schema);
            }
        }

        updateSchemaForClass(schema, className);

        return result;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static void updateSchemaForClass(final TypeSchema schema, final String className) {
        switch (className) {
            case "java.lang.String":
                schema.put(TYPE_PROP, "string");
                return;
            case "char":
            case "java.lang.Character":
                schema.put(TYPE_PROP, "string");
                schema.put(FORMAT_PROP, "uint16");
                return;
            case "byte":
            case "java.lang.Byte":
                schema.put(TYPE_PROP, INTEGER_TYPE);
                schema.put(FORMAT_PROP, "int8");
                return;
            case "short":
            case "java.lang.Short":
                schema.put(TYPE_PROP, INTEGER_TYPE);
                schema.put(FORMAT_PROP, "int16");
                return;
            case "int":
            case "java.lang.Integer":
                schema.put(TYPE_PROP, INTEGER_TYPE);
                schema.put(FORMAT_PROP, "int32");
                return;
            case "long":
            case "java.lang.Long":
                schema.put(TYPE_PROP, INTEGER_TYPE);
                schema.put(FORMAT_PROP, "int64");
                return;
            case "double":
            case "java.lang.Double":
                schema.put(TYPE_PROP, "number");
                schema.put(FORMAT_PROP, "double");
                return;
            case "float":
            case "java.lang.Float":
                schema.put(TYPE_PROP, "number");
                schema.put(FORMAT_PROP, "float");
                return;
            case "boolean":
            case "java.lang.Boolean":
                schema.put(TYPE_PROP, "boolean");
                return;
            default:
                schema.put("$ref", "#/components/schemas/" + className.substring(className.lastIndexOf('.') + 1));
        }
    }

    /**
     * Validates the object against this schema.
     *
     * @param obj
     */
    public void validate(final JSONObject obj) {
        // get the components bit of the main metadata

        final JSONObject toValidate = new JSONObject();
        toValidate.put("prop", obj);

        JSONObject schemaJSON;
        if (this.containsKey(SCHEMA_PROP)) {
            schemaJSON = new JSONObject((Map) this.get(SCHEMA_PROP));
        } else {
            schemaJSON = new JSONObject(this);
        }

        final JSONObject rawSchema = new JSONObject();
        rawSchema.put("properties", new JSONObject().put("prop", schemaJSON));
        rawSchema.put("components", new JSONObject().put("schemas", MetadataBuilder.getComponents()));
        final Schema schema = SchemaLoader.load(rawSchema);
        try {
            schema.validate(toValidate);
        } catch (final ValidationException e) {
            final StringBuilder sb = new StringBuilder("Validation Errors::");
            e.getCausingExceptions().stream()
                    .map(ValidationException::getMessage)
                    .forEach(sb::append);
            String message = sb.toString();
            LOGGER.info(message);
            throw new ContractRuntimeException(message, e);
        }
    }
}
