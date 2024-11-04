/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Used as the default serialisation for transmission from SDK to Contract. */
@Serializer()
@SuppressWarnings({"PMD.GodClass", "PMD.AvoidLiteralsInIfCondition", "PMD.AvoidDuplicateLiterals"})
public class JSONTransactionSerializer implements SerializerInterface {
    private static final Logger LOGGER = Logger.getLogger(JSONTransactionSerializer.class.getName());
    private final TypeRegistry typeRegistry = TypeRegistry.getRegistry();

    /**
     * Convert the value supplied to a byte array, according to the TypeSchema.
     *
     * @param value
     * @param ts
     * @return Byte buffer
     */
    @Override
    public byte[] toBuffer(final Object value, final TypeSchema ts) {
        LOGGER.debug(() -> "Schema to convert is " + ts);
        byte[] buffer = null;
        if (value != null) {
            final String type = ts.getType();
            if (type != null) {
                switch (type) {
                    case "array":
                        final JSONArray array = normalizeArray(new JSONArray(value), ts);
                        buffer = array.toString().getBytes(UTF_8);
                        break;
                    case "string":
                        final String format = ts.getFormat();
                        if ("utin16".equals(format)) {
                            buffer = Character.valueOf((char) value).toString().getBytes(UTF_8);
                        } else {
                            buffer = ((String) value).getBytes(UTF_8);
                        }
                        break;
                    case "number":
                    case "integer":
                    case "boolean":
                    default:
                        buffer = value.toString().getBytes(UTF_8);
                }
            } else {
                // at this point we can assert that the value is
                // representing a complex data type
                // so we can get this from
                // the type registry, and get the list of propertyNames
                // it should have
                final DataTypeDefinition dtd = this.typeRegistry.getDataType(ts);
                final Set<String> keySet = dtd.getProperties().keySet();
                final String[] propNames = keySet.toArray(new String[0]);

                // Note: whilst the current JSON library does pretty much
                // everything is required, this part is hard.
                // we want to create a JSON Object based on the value,
                // with certain property names.

                // Based on the constructors available we need to have a two
                // step process, create a JSON Object, then create the object
                // we really want based on the propNames
                final JSONObject obj = new JSONObject(new JSONObject(value), propNames);
                buffer = obj.toString().getBytes(UTF_8);
            }
        }
        return buffer;
    }

    /**
     * Normalize the Array.
     *
     * <p>We need to take the JSON array, and if there are complex datatypes within it ensure that they don't get
     * spurious JSON properties appearing
     *
     * <p>This method needs to be general so has to copy with nested arrays and with primitive and Object types
     *
     * @param jsonArray incoming array
     * @param ts Schema to normalise to
     * @return JSONArray
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private JSONArray normalizeArray(final JSONArray jsonArray, final TypeSchema ts) {
        JSONArray normalizedArray;

        // Need to work with what type of array this is
        final TypeSchema items = ts.getItems();
        final String type = items.getType();

        if (null == type) {
            // get the permitted propeties in the type,
            // then loop over the array and ensure they are correct
            final DataTypeDefinition dtd = this.typeRegistry.getDataType(items);
            final Set<String> keySet = dtd.getProperties().keySet();
            final String[] propNames = keySet.toArray(new String[0]);

            normalizedArray = new JSONArray();
            // array of objects
            // iterate over said array
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject obj = new JSONObject(jsonArray.getJSONObject(i), propNames);
                normalizedArray.put(i, obj);
            }
        } else if ("array".equals(type)) {
            // nested arrays, get the type of what it makes up
            // Need to loop over all elements and normalize each one
            normalizedArray = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                normalizedArray.put(i, normalizeArray(jsonArray.getJSONArray(i), items));
            }
        } else {
            // primitive - can return this directly
            normalizedArray = jsonArray;
        }

        return normalizedArray;
    }

    /**
     * Take the byte buffer and return the object as required.
     *
     * @param buffer Byte buffer from the wire
     * @param ts TypeSchema representing the type
     * @return Object created; relies on Java auto-boxing for primitives
     */
    @Override
    public Object fromBuffer(final byte[] buffer, final TypeSchema ts) {
        try {
            final String stringData = new String(buffer, UTF_8);
            return convert(stringData, ts);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ContractRuntimeException(e);
        }
    }

    /**
     * We need to be able to map between the primative class types and the object variants. In the case where this is
     * needed Java auto-boxing doesn't actually help.
     *
     * <p>For other types the parameter is passed directly back
     *
     * @param primitive class for the primitive
     * @return Class for the Object variant
     */
    private Class<?> mapPrimitive(final Class<?> primitive) {
        if (primitive.isArray()) {
            return mapArrayPrimitive(primitive);
        }

        return mapBasicPrimitive(primitive);
    }

    private Class<?> mapArrayPrimitive(final Class<?> primitive) {
        switch (primitive.getComponentType().getName()) {
            case "int":
                return Integer[].class;
            case "long":
                return Long[].class;
            case "float":
                return Float[].class;
            case "double":
                return Double[].class;
            case "short":
                return Short[].class;
            case "byte":
                return Byte[].class;
            case "char":
                return Character[].class;
            case "boolean":
                return Boolean[].class;
            default:
                return primitive;
        }
    }

    private Class<?> mapBasicPrimitive(final Class<?> primitive) {
        switch (primitive.getName()) {
            case "int":
                return Integer.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "short":
                return Short.class;
            case "byte":
                return Byte.class;
            case "char":
                return Character.class;
            case "boolean":
                return Boolean.class;
            default:
                return primitive;
        }
    }

    /** Internal method to do the conversion */
    private Object convert(final String stringData, final TypeSchema ts)
            throws IllegalAccessException, InstantiationException {
        LOGGER.debug(() -> "Schema to convert is " + ts);

        String type = ts.getType();

        String format = null;
        if (type == null) {
            type = "object";
            final String ref = ts.getRef();
            format = ref.substring(ref.lastIndexOf('/') + 1);
        }

        switch (type) {
            case "string":
                return convertString(stringData, ts);
            case "integer":
                return convertInteger(stringData, ts);
            case "number":
                return convertNumber(stringData, ts);
            case "boolean":
                return Boolean.parseBoolean(stringData);
            case "object":
                return createComponentInstance(format, stringData, ts);
            case "array":
                return convertArray(stringData, ts);
            default:
                return null;
        }
    }

    private Object convertArray(final String stringData, final TypeSchema ts)
            throws IllegalAccessException, InstantiationException {
        final JSONArray jsonArray = new JSONArray(stringData);
        final TypeSchema itemSchema = ts.getItems();

        // note here that the type has to be converted in the case of primitives
        final Object[] data = (Object[])
                Array.newInstance(mapPrimitive(itemSchema.getTypeClass(this.typeRegistry)), jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            final Object convertedData = convert(jsonArray.get(i).toString(), itemSchema);
            data[i] = convertedData;
        }

        return data;
    }

    private Object convertNumber(final String stringData, final TypeSchema ts) {
        if ("float".equals(ts.getFormat())) {
            return Float.parseFloat(stringData);
        }

        return Double.parseDouble(stringData);
    }

    private Object convertInteger(final String stringData, final TypeSchema ts) {
        switch (ts.getFormat()) {
            case "int32":
                return Integer.parseInt(stringData);
            case "int8":
                return Byte.parseByte(stringData);
            case "int16":
                return Short.parseShort(stringData);
            case "int64":
                return Long.parseLong(stringData);
            default:
                throw new IllegalArgumentException("Unknown format for integer " + ts.getFormat());
        }
    }

    private Object convertString(final String stringData, final TypeSchema ts) {
        if ("uint16".equals(ts.getFormat())) {
            return stringData.charAt(0);
        }

        return stringData;
    }

    /**
     * Create new instance of the specificied object from the supplied JSON String.
     *
     * @param format Details of the format needed
     * @param jsonString JSON string
     * @param ts TypeSchema
     * @return new object
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    Object createComponentInstance(final String format, final String jsonString, final TypeSchema ts) {

        final DataTypeDefinition dtd = this.typeRegistry.getDataType(format);
        Object obj;
        try {
            obj = dtd.getTypeClass().getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException
                | InstantiationException
                | InvocationTargetException
                | NoSuchMethodException e1) {
            throw new ContractRuntimeException("Unable to to create new instance of type", e1);
        }

        final JSONObject json = new JSONObject(jsonString);
        // request validation of the type may throw an exception if validation fails
        ts.validate(json);
        try {
            final Map<String, PropertyDefinition> fields = dtd.getProperties();
            for (final PropertyDefinition prop : fields.values()) {
                final Field f = prop.getField();
                f.setAccessible(true);
                final Object newValue = convert(json.get(prop.getName()).toString(), prop.getSchema());

                f.set(obj, newValue);
            }
            return obj;
        } catch (SecurityException
                | IllegalArgumentException
                | IllegalAccessException
                | InstantiationException
                | JSONException e) {
            throw new ContractRuntimeException("Unable to convert JSON to object", e);
        }
    }
}
