/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.metadata;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.json.JSONObject;

/**
 *
 * Custom sub-type of Map that helps with the case where if there's no value
 * then do not insert the property at all
 *
 * Does not include the "schema" top level map
 */
@SuppressWarnings("serial")
public class TypeSchema extends HashMap<String, Object> {
    private static Logger logger = Logger.getLogger(TypeSchema.class.getName());

    public TypeSchema() {

    }

    private Object _putIfNotNull(String key, Object value) {
        if (value != null && !value.toString().isEmpty()) {
            return put(key, value);
        } else {
            return null;
        }
    }

    String putIfNotNull(String key, String value) {
        return (String) this._putIfNotNull(key, value);
    }

    String[] putIfNotNull(String key, String[] value) {
        return (String[]) this._putIfNotNull(key, value);
    }

    TypeSchema putIfNotNull(String key, TypeSchema value) {
        return (TypeSchema) this._putIfNotNull(key, value);
    }

    TypeSchema[] putIfNotNull(String key, TypeSchema[] value) {
        return (TypeSchema[]) this._putIfNotNull(key, value);
    }

    public String getType() {
        if (this.containsKey("schema")) {
            Map<?, ?> intermediateMap = (Map<?, ?>) this.get("schema");
            return (String) intermediateMap.get("type");
        }
        return (String) this.get("type");
    }

    public TypeSchema getItems() {
        if (this.containsKey("schema")) {
            Map<?, ?> intermediateMap = (Map<?, ?>) this.get("schema");
            return (TypeSchema) intermediateMap.get("items");
        }
        return (TypeSchema) this.get("items");
    }

    public String getRef() {
        if (this.containsKey("schema")) {
            Map<?, ?> intermediateMap = (Map<?, ?>) this.get("schema");
            return (String) intermediateMap.get("$ref");
        }
        return (String) this.get("$ref");

    }

    public String getFormat() {
        if (this.containsKey("schema")) {
            Map<?, ?> intermediateMap = (Map<?, ?>) this.get("schema");
            return (String) intermediateMap.get("format");
        }
        return (String) this.get("format");
    }

    public Class<?> getTypeClass(TypeRegistry typeRegistry) {
        Class<?> clz = null;
        String type = getType();
        if (type == null) {
            type = "object";
        }

        if (type.contentEquals("string")) {
            clz = String.class;
        } else if (type.contentEquals("integer")) {
            // need to check the format
            String format = getFormat();
            switch(format) {
                case "int8":
                  clz = byte.class;
                  break;
                case "int16":
                  clz = short.class;
                  break;
                case "int32":
                  clz = int.class;
                  break;
                case "int64":
                  clz = long.class;
                  break;
                default:
                  throw new RuntimeException("Unkown format for integer of "+format);
            }
        } else if (type.contentEquals("number")) {
            // need to check the format
            String format = getFormat();
            switch(format) {
                case "double":
                  clz = double.class;
                  break;
                case "float":
                  clz = float.class;
                  break;
                default:
                  throw new RuntimeException("Unkown format for number of "+format);
            }
        } else if (type.contentEquals("boolean")) {
            clz = boolean.class;
        } else if (type.contentEquals("object")) {
            String ref = this.getRef();
            String format = ref.substring(ref.lastIndexOf("/") + 1);
            clz = typeRegistry.getDataType(format).getTypeClass();
        } else if (type.contentEquals("array")) {
            TypeSchema typdef = this.getItems();
            Class<?> arrayType = typdef.getTypeClass(typeRegistry);
            clz = Array.newInstance(arrayType, 0).getClass();
        }

        return clz;
    }

    /**
     * Provide a mapping between the Java Language types and the OpenAPI based types
     *
     */
    public static TypeSchema typeConvert(Class<?> clz) {
        TypeSchema returnschema = new TypeSchema();
        String className = clz.getTypeName();
        if (className == "void") {
            return null;
        }

        TypeSchema schema;

        if (clz.isArray()) {
            returnschema.put("type", "array");
            schema = new TypeSchema();
            
            // double check the componentType
            Class<?> componentClass = clz.getComponentType();
            if (componentClass.isArray()){
                // nested arrays
                returnschema.put("items",TypeSchema.typeConvert(componentClass));
            } else {
                returnschema.put("items", schema);
            }
            
            className = componentClass.getTypeName();
        } else {
            schema = returnschema;
        }

        switch (className) {
        case "java.lang.String":
            schema.put("type", "string");
            break;
        case "byte":
        case "java.lang.Byte":
            schema.put("type", "integer");
            schema.put("format", "int8");
            break;
        case "short":
        case "java.lang.Short":
            schema.put("type", "integer");
            schema.put("format", "int16");
            break;
        case "int":
        case "java.lang.Integer":
            schema.put("type", "integer");
            schema.put("format", "int32");
            break;
        case "long":
        case "java.lang.Long":
            schema.put("type", "integer");
            schema.put("format", "int64");
            break;
        case "double":
        case "java.lang.Double":
            schema.put("type", "number");
            schema.put("format", "double");
            break;
        case "float":
        case "java.lang.Float":
            schema.put("type", "number");
            schema.put("format", "float");
            break;
        case "boolean":
        case "java.lang.Boolean":
            schema.put("type", "boolean");
            break;
        default:
            schema.put("$ref", "#/components/schemas/" + className.substring(className.lastIndexOf('.') + 1));
        }

        return returnschema;
    }

    public void validate(JSONObject obj) {
        // get the components bit of the main metadata

        JSONObject toValidate = new JSONObject();
        toValidate.put("prop", obj);

        JSONObject schemaJSON;
        if (this.containsKey("schema")) {
            schemaJSON = new JSONObject((Map) this.get("schema"));
        } else {
            schemaJSON = new JSONObject(this);
        }

        JSONObject rawSchema = new JSONObject();
        rawSchema.put("properties", new JSONObject().put("prop", schemaJSON));
        rawSchema.put("components", new JSONObject().put("schemas", MetadataBuilder.getComponents()));
        Schema schema = SchemaLoader.load(rawSchema);
        try {
            schema.validate(toValidate);
        } catch (ValidationException e) {
            StringBuilder sb = new StringBuilder("Validation Errors::");
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(sb::append);
            logger.info(sb.toString());
            throw new ContractRuntimeException(sb.toString(), e);
        }

    }

}
