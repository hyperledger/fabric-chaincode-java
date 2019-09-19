/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used as a the default serialisation for transmission from SDK to Contract
 */
public class JSONTransactionSerializer {
    private static Logger logger = Logger.getLogger(JSONTransactionSerializer.class.getName());
    private TypeRegistry typeRegistry;

    /**
     * Create a new serialiser and maintain a reference to the TypeRegistry
     *
     * @param typeRegistry
     */
    public JSONTransactionSerializer(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    /**
     * Convert the value supplied to a byte array, according to the TypeSchema
     *
     * @param value
     * @param ts
     * @return  Byte buffer
     */
    public byte[] toBuffer(Object value, TypeSchema ts) {
        logger.debug(() -> "Schema to convert is " + ts);
        byte[] buffer = null;
        if (value != null) {
            String type = ts.getType();
            if (type != null) {
                switch (type) {
                case "array":
                    JSONArray array = new JSONArray(value);
                    buffer = array.toString().getBytes(UTF_8);
                    break;
                case "string":
                    buffer = ((String) value).getBytes(UTF_8);
                    break;
                case "number":
                case "integer":
                case "boolean":
                default:
                    buffer = (value).toString().getBytes(UTF_8);
                }
            } else {
                JSONObject obj = new JSONObject(value);
                buffer = obj.toString().getBytes(UTF_8);
            }
        }
        return buffer;
    }

    /**
     * Take the byte buffer and return the object as required
     *
     * @param buffer Byte buffer from the wire
     * @param ts     TypeSchema representing the type
     *
     * @return Object created; relies on Java auto-boxing for primitives
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object fromBuffer(byte[] buffer, TypeSchema ts) {
        try {
            String stringData = new String(buffer, StandardCharsets.UTF_8);
            Object value = null;

            value = _convert(stringData, ts);

            return value;
        } catch (InstantiationException | IllegalAccessException e) {
            ContractRuntimeException cre = new ContractRuntimeException(e);
            throw cre;
        }
    }

    /*
     * Internal method to do the conversion
     */
    private Object _convert(String stringData, TypeSchema ts)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        logger.debug(() -> "Schema to convert is " + ts);
        String type = ts.getType();
        String format = null;
        Object value = null;
        if (type == null) {
            type = "object";
            String ref = ts.getRef();
            format = ref.substring(ref.lastIndexOf("/") + 1);
        }

        if (type.contentEquals("string")) {
            value = stringData;
        } else if (type.contentEquals("integer")) {
            String intFormat = ts.getFormat();
            if (intFormat.contentEquals("int32")) {
                value = Integer.parseInt(stringData);
            } else {
                value = Long.parseLong(stringData);
            }
        } else if (type.contentEquals("number")) {
            String numFormat = ts.getFormat();
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
            JSONArray jsonArray = new JSONArray(stringData);
            TypeSchema itemSchema = ts.getItems();
            Object[] data = (Object[]) Array.newInstance(itemSchema.getTypeClass(this.typeRegistry),
                    jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                data[i] = _convert(jsonArray.get(i).toString(), itemSchema);
            }
            value = data;

        }
        return value;
    }

    Object createComponentInstance(String format, String jsonString, TypeSchema ts) {

        DataTypeDefinition dtd = this.typeRegistry.getDataType(format);
        Object obj;
        try {
            obj = dtd.getTypeClass().getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e1) {
            throw new ContractRuntimeException("Unable to to create new instance of type", e1);
        }

        JSONObject json = new JSONObject(jsonString);
        // request validation of the type may throw an exception if validation fails
        ts.validate(json);

        try {
            Map<String, PropertyDefinition> fields = dtd.getProperties();
            for (Iterator<PropertyDefinition> iterator = fields.values().iterator(); iterator.hasNext();) {
                PropertyDefinition prop = iterator.next();

                Field f = prop.getField();
                f.setAccessible(true);
                Object newValue = _convert(json.get(prop.getName()).toString(), prop.getSchema());

                f.set(obj, newValue);

            }
            return obj;
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException
                | JSONException e) {
            throw new ContractRuntimeException("Unable to convert JSON to object", e);
        }

    }
}
