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
import java.util.Set;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.hyperledger.fabric.contract.annotation.Serializer;

/**
 * Used as a the default serialisation for transmission from SDK to Contract
 */
@Serializer() 
public class JSONTransactionSerializer implements SerializerInterface {
    private static Logger logger = Logger.getLogger(JSONTransactionSerializer.class.getName());
    private TypeRegistry typeRegistry = TypeRegistry.getRegistry();

    /**
     * Create a new serialiser
     */
    public JSONTransactionSerializer() {
    }

    /**
     * Convert the value supplied to a byte array, according to the TypeSchema
     *
     * @param value
     * @param ts
     * @return  Byte buffer
     */
    @Override
    public byte[] toBuffer(Object value, TypeSchema ts) {
        logger.debug(() -> "Schema to convert is " + ts);
        byte[] buffer = null;
        if (value != null) {
            String type = ts.getType();
            if (type != null) {
                switch (type) {
                case "array":
                    JSONArray array = normalizeArray(new JSONArray(value),ts);
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
                // at this point we can assert that the value is 
                // representing a complex data type
                // so we can get this from 
                // the type registry, and get the list of propertyNames 
                // it should have
                DataTypeDefinition dtd = this.typeRegistry.getDataType(ts);
                Set<String> keySet = dtd.getProperties().keySet();
                String[] propNames = keySet.toArray(new String[keySet.size()]);
                
                // Note: whilst the current JSON library does pretty much
                // everything is required, this part is hard.
                // we want to create a JSON Object based on the value, 
                // with certain property names.

                // Based on the constructors available we need to have a two 
                // step process, create a JSON Object, then create the object
                // we really want based on the propNames
                JSONObject obj = new JSONObject(new JSONObject(value),propNames);
                buffer = obj.toString().getBytes(UTF_8);
            }
        }
        return buffer;
    }

    /**
     * We need to take the JSON array, and if there are complex datatypes within it
     * ensure that they don't get spurious JSON properties appearing
     * 
     * This method needs to be general so has to copy with nested arrays
     * and with primitive and Object types
     */
    private JSONArray normalizeArray(JSONArray jsonArray, TypeSchema ts){
        JSONArray normalizedArray;
        
        // Need to work with what type of array this is
        TypeSchema items = ts.getItems();
        String type = items.getType();
        
        if (type != null && type != "array" ){
            // primitive - can return this directly
            normalizedArray = jsonArray;
        } else if ( type != null && type == "array") {
            // nested arrays, get the type of what it makes up
            // Need to loop over all elements and normalize each one
            normalizedArray = new JSONArray();
            for (int i=0; i<jsonArray.length(); i++){
                normalizedArray.put(i,normalizeArray(jsonArray.getJSONArray(i),items));
            }
        } else {
            // get the permitted propeties in the type, 
            // then loop over the array and ensure they are correct
            DataTypeDefinition dtd = this.typeRegistry.getDataType(items);
            Set<String> keySet = dtd.getProperties().keySet();
            String[] propNames = keySet.toArray(new String[keySet.size()]);
          
            normalizedArray = new JSONArray();
            // array of objects
            // iterate over said array
            for (int i=0; i<jsonArray.length(); i++){         
                JSONObject obj = new JSONObject(jsonArray.getJSONObject(i),propNames);
                normalizedArray.put(i,obj);
            }

        }
        return normalizedArray;
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
    @Override
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

    /** We need to be able to map between the primative class types
     * and the object variants. In the case where this is needed
     * Java auto-boxing doesn't actually help.
     * 
     * For other types the parameter is passed directly back
     * 
     * @param primitive class for the primitive 
     * @return Class for the Object variant
     */
    private Class<?> mapPrimitive(Class<?> primitive){
        String primitiveType;
        boolean isArray = primitive.isArray();
        if (isArray){
            primitiveType = primitive.getComponentType().getName();
        } else {
            primitiveType = primitive.getName();
        }

        switch (primitiveType) {
            case "int":
                return isArray ? Integer[].class : Integer.class;
            case "long":
                return isArray ? Long[].class: Long.class;
            case "float":
                return isArray ? Float[].class:Float.class;
            case "double":
                return isArray ? Double[].class:Double.class;
            case "short":
                return isArray ? Short[].class:Short.class;
            case "byte":
                return isArray ? Byte[].class:Byte.class;
            case "char":
                return isArray ? Character[].class:Character.class;
            case "boolean":
                return isArray ? Boolean[].class:Boolean.class;
             default:
                return primitive;
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
            } else if (intFormat.contentEquals("int8")) {
                value = Byte.parseByte(stringData);
            } else if (intFormat.contentEquals("int16")){
                value = Short.parseShort(stringData);
            } else if (intFormat.contentEquals("int64")){
                value = Long.parseLong(stringData);
            } else {
                throw new RuntimeException("Unknown format for integer "+intFormat);
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

            // note here that the type has to be converted in the case of primitives
            Object[] data = (Object[]) Array.newInstance(mapPrimitive(itemSchema.getTypeClass(this.typeRegistry)),
                    jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                Object convertedData = _convert(jsonArray.get(i).toString(), itemSchema);
                data[i] = convertedData;
            }
            value = data;

        }
        return value;
    }

    /** Create new instance of the specificied object from the supplied JSON String
     * 
     * @param format      Details of the format needed
     * @param jsonString  JSON string
     * @param ts          TypeSchema
     * @return new object
     */
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
