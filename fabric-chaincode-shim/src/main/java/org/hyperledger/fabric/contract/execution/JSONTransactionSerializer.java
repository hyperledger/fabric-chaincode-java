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
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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

/**
 * Used as a the default serialisation for transmission from SDK to Contract.
 */
@Serializer()
public class JSONTransactionSerializer implements SerializerInterface {
    private static Logger logger = Logger.getLogger(JSONTransactionSerializer.class.getName());
    private final TypeRegistry typeRegistry = TypeRegistry.getRegistry();

    /**
     * Create a new serialiser.
     */
    public JSONTransactionSerializer() {
    }

    /**
     * Convert the value supplied to a byte array, according to the TypeSchema.
     *
     * @param value
     * @param ts
     * @return Byte buffer
     */
    @Override
    public byte[] toBuffer(final Object value, final TypeSchema ts) {
        logger.debug(() -> "Schema to convert is " + ts);
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
                    if (format != null && format.contentEquals("uint16")) {
                        buffer = Character.valueOf((char) value).toString().getBytes(UTF_8);
                    } else {
                        buffer = ((String) value).getBytes(UTF_8);
                    }
                    break;
                case "number":
                case "integer":
                case "boolean":
                default:
                    buffer = (value).toString().getBytes(UTF_8);
                }
            } else {
                // at this point we can assert that the value is
                // representing a complex data type
                // so we can get this from
                // the type registry, and get the list of propertyNames
                // it should have
                final DataTypeDefinition dtd = this.typeRegistry.getDataType(ts);
                final Set<String> keySet = dtd.getProperties().keySet();
                final String[] propNames = keySet.toArray(new String[keySet.size()]);

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
     * We need to take the JSON array, and if there are complex datatypes within it
     * ensure that they don't get spurious JSON properties appearing
     *
     * This method needs to be general so has to copy with nested arrays and with
     * primitive and Object types
     *
     * @param jsonArray incoming array
     * @param ts        Schema to normalise to
     * @return JSONArray
     */
    private JSONArray normalizeArray(final JSONArray jsonArray, final TypeSchema ts) {
        JSONArray normalizedArray;

        // Need to work with what type of array this is
        final TypeSchema items = ts.getItems();
        final String type = items.getType();

        if (type != null && type != "array") {
            // primitive - can return this directly
            normalizedArray = jsonArray;
        } else if (type != null && type == "array") {
            // nested arrays, get the type of what it makes up
            // Need to loop over all elements and normalize each one
            normalizedArray = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                normalizedArray.put(i, normalizeArray(jsonArray.getJSONArray(i), items));
            }
        } else {
            // get the permitted propeties in the type,
            // then loop over the array and ensure they are correct
            final DataTypeDefinition dtd = this.typeRegistry.getDataType(items);
            final Set<String> keySet = dtd.getProperties().keySet();
            final String[] propNames = keySet.toArray(new String[keySet.size()]);

            normalizedArray = new JSONArray();
            // array of objects
            // iterate over said array
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject obj = new JSONObject(jsonArray.getJSONObject(i), propNames);
                normalizedArray.put(i, obj);
            }

        }
        return normalizedArray;
    }

    /**
     * Take the byte buffer and return the object as required.
     *
     * @param buffer Byte buffer from the wire
     * @param ts     TypeSchema representing the type
     *
     * @return Object created; relies on Java auto-boxing for primitives
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Override
    public Object fromBuffer(final byte[] buffer, final TypeSchema ts) {
        try {
            final String stringData = new String(buffer, StandardCharsets.UTF_8);
            Object value = null;

            value = convert(stringData, ts);

            return value;
        } catch (InstantiationException | IllegalAccessException e) {
            final ContractRuntimeException cre = new ContractRuntimeException(e);
            throw cre;
        }
    }

    /**
     * We need to be able to map between the primative class types and the object
     * variants. In the case where this is needed Java auto-boxing doesn't actually
     * help.
     *
     * For other types the parameter is passed directly back
     *
     * @param primitive class for the primitive
     * @return Class for the Object variant
     */
    private Class<?> mapPrimitive(final Class<?> primitive) {
        String primitiveType;
        final boolean isArray = primitive.isArray();
        if (isArray) {
            primitiveType = primitive.getComponentType().getName();
        } else {
            primitiveType = primitive.getName();
        }

        switch (primitiveType) {
        case "int":
            return isArray ? Integer[].class : Integer.class;
        case "long":
            return isArray ? Long[].class : Long.class;
        case "float":
            return isArray ? Float[].class : Float.class;
        case "double":
            return isArray ? Double[].class : Double.class;
        case "short":
            return isArray ? Short[].class : Short.class;
        case "byte":
            return isArray ? Byte[].class : Byte.class;
        case "char":
            return isArray ? Character[].class : Character.class;
        case "boolean":
            return isArray ? Boolean[].class : Boolean.class;
        default:
            return primitive;
        }
    }

    /*
     * Internal method to do the conversion
     */
    private Object convert(final String stringData, final TypeSchema ts) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        logger.debug(() -> "Schema to convert is " + ts);
        String type = ts.getType();
        String format = null;
        Object value = null;
        if (type == null) {
            type = "object";
            final String ref = ts.getRef();
            format = ref.substring(ref.lastIndexOf("/") + 1);
        }

        if (type.contentEquals("string")) {
            final String strformat = ts.getFormat();
            if (strformat != null && strformat.contentEquals("uint16")) {
                value = stringData.charAt(0);
            } else {
                value = stringData;
            }
        } else if (type.contentEquals("integer")) {
            final String intFormat = ts.getFormat();
            switch (intFormat) {
            case "int32":
                value = Integer.parseInt(stringData);
                break;
            case "int8":
                value = Byte.parseByte(stringData);
                break;
            case "int16":
                value = Short.parseShort(stringData);
                break;
            case "int64":
                value = Long.parseLong(stringData);
                break;
            default:
                throw new RuntimeException("Unknown format for integer " + intFormat);
            }

        } else if (type.contentEquals("number")) {
            final String numFormat = ts.getFormat();
            if (numFormat.contentEquals("float")) {
                value = Float.parseFloat(stringData);
            } else {
                value = Double.parseDouble(stringData);
            }
        } else if (type.contentEquals("boolean")) {
            value = Boolean.parseBoolean(stringData);
        } else if (type.contentEquals("object")) {
            value = createComponentInstance(format, stringData, ts);
        } else if (type.contentEquals("array")) {
            final JSONArray jsonArray = new JSONArray(stringData);
            final TypeSchema itemSchema = ts.getItems();

            // note here that the type has to be converted in the case of primitives
            final Object[] data = (Object[]) Array.newInstance(mapPrimitive(itemSchema.getTypeClass(this.typeRegistry)), jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                final Object convertedData = convert(jsonArray.get(i).toString(), itemSchema);
                data[i] = convertedData;
            }
            value = data;

        }
        return value;
    }

    /**
     * Create new instance of the specificied object from the supplied JSON String.
     *
     * @param format     Details of the format needed
     * @param jsonString JSON string
     * @param ts         TypeSchema
     * @return new object
     */
    Object createComponentInstance(final String format, final String jsonString, final TypeSchema ts) {

        final DataTypeDefinition dtd = this.typeRegistry.getDataType(format);
        Object obj;
        try {
            obj = dtd.getTypeClass().getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e1) {
            throw new ContractRuntimeException("Unable to to create new instance of type", e1);
        }

        final JSONObject json = new JSONObject(jsonString);
        // request validation of the type may throw an exception if validation fails
        ts.validate(json);
        try {
            final Map<String, PropertyDefinition> fields = dtd.getProperties();
            for (final Iterator<PropertyDefinition> iterator = fields.values().iterator(); iterator.hasNext();) {
                final PropertyDefinition prop = iterator.next();

                final Field f = prop.getField();
                f.setAccessible(true);
                final Object newValue = convert(json.get(prop.getName()).toString(), prop.getSchema());

                f.set(obj, newValue);

            }
            return obj;
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | JSONException e) {
            throw new ContractRuntimeException("Unable to convert JSON to object", e);
        }

    }
}
