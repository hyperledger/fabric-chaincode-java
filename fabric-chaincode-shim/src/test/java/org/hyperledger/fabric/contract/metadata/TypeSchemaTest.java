/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.metadata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TypeSchemaTest {

    @Before
    public void beforeEach() {
    }

    @Test
    public void putIfNotNull() {
        final TypeSchema ts = new TypeSchema();

        System.out.println("Key - value");
        ts.putIfNotNull("Key", "value");

        System.out.println("Key - null");
        final String nullstr = null;
        ts.putIfNotNull("Key", nullstr);

        assertThat(ts.get("Key"), equalTo("value"));

        System.out.println("Key - <empty>");
        ts.putIfNotNull("Key", "");

        assertThat(ts.get("Key"), equalTo("value"));
    }

    @Test
    public void getType() {
        final TypeSchema ts = new TypeSchema();
        ts.put("type", "MyType");
        assertThat(ts.getType(), equalTo("MyType"));

        final TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getType(), equalTo("MyType"));
    }

    @Test
    public void getFormat() {
        final TypeSchema ts = new TypeSchema();
        ts.put("format", "MyFormat");
        assertThat(ts.getFormat(), equalTo("MyFormat"));

        final TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getFormat(), equalTo("MyFormat"));
    }

    @Test
    public void getRef() {
        final TypeSchema ts = new TypeSchema();
        ts.put("$ref", "#/ref/to/MyType");
        assertThat(ts.getRef(), equalTo("#/ref/to/MyType"));

        final TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getRef(), equalTo("#/ref/to/MyType"));
    }

    @Test
    public void getItems() {
        final TypeSchema ts1 = new TypeSchema();

        final TypeSchema ts = new TypeSchema();
        ts.put("items", ts1);
        assertThat(ts.getItems(), equalTo(ts1));

        final TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getItems(), equalTo(ts1));
    }

    @DataType
    class MyType {
    }

    @Test
    public void getTypeClass() {
        final TypeSchema ts = new TypeSchema();

        ts.put("type", "string");
        final TypeRegistry mockRegistry = new TypeRegistryImpl();
        assertThat(ts.getTypeClass(mockRegistry), equalTo(String.class));

        ts.put("type", "integer");
        ts.put("format", "int8");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(byte.class));

        ts.put("type", "integer");
        ts.put("format", "int16");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(short.class));

        ts.put("type", "integer");
        ts.put("format", "int32");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(int.class));

        ts.put("type", "integer");
        ts.put("format", "int64");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(long.class));

        ts.put("type", "number");
        ts.put("format", "double");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(double.class));

        ts.put("type", "number");
        ts.put("format", "float");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(float.class));

        ts.put("type", "boolean");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(boolean.class));

        ts.put("type", null);
        ts.put("$ref", "#/ref/to/MyType");

        mockRegistry.addDataType(MyType.class);
        assertThat(ts.getTypeClass(mockRegistry), equalTo(MyType.class));

        final TypeSchema array = new TypeSchema();
        array.put("type", "array");
        array.put("items", ts);
        assertThat(array.getTypeClass(mockRegistry), equalTo(MyType[].class));

    }

    @Test
    public void unknownConversions() {
        assertThrows(RuntimeException.class, () -> {
            final TypeSchema ts = new TypeSchema();
            final TypeRegistry mockRegistry = new TypeRegistryImpl();
            ts.put("type", "integer");
            ts.put("format", "int63");
            ts.getTypeClass(mockRegistry);
        });

        assertThrows(RuntimeException.class, () -> {
            final TypeSchema ts = new TypeSchema();
            final TypeRegistry mockRegistry = new TypeRegistryImpl();
            ts.put("type", "number");
            ts.put("format", "approximate");
            ts.getTypeClass(mockRegistry);
        });
    }

    @Test
    public void typeConvertPrimitives() {
        TypeSchema rts;

        final String[] array = new String[] {};
        rts = TypeSchema.typeConvert(array.getClass());
        assertThat(rts.getType(), equalTo("array"));

        rts = TypeSchema.typeConvert(int.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(long.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(float.class);
        assertThat(rts.getType(), equalTo("number"));

        rts = TypeSchema.typeConvert(double.class);
        assertThat(rts.getType(), equalTo("number"));

        rts = TypeSchema.typeConvert(byte.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(short.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(boolean.class);
        assertThat(rts.getType(), equalTo("boolean"));

    }

    @Test
    public void typeConvertObjects() {
        TypeSchema rts;
        rts = TypeSchema.typeConvert(String.class);
        assertThat(rts.getType(), equalTo("string"));

        final String[] array = new String[] {};
        rts = TypeSchema.typeConvert(array.getClass());
        assertThat(rts.getType(), equalTo("array"));

        rts = TypeSchema.typeConvert(Integer.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(Long.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(Float.class);
        assertThat(rts.getType(), equalTo("number"));

        rts = TypeSchema.typeConvert(Double.class);
        assertThat(rts.getType(), equalTo("number"));

        rts = TypeSchema.typeConvert(Byte.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(Short.class);
        assertThat(rts.getType(), equalTo("integer"));

        rts = TypeSchema.typeConvert(Boolean.class);
        assertThat(rts.getType(), equalTo("boolean"));

        rts = TypeSchema.typeConvert(MyType.class);
        assertThat(rts.getRef(), equalTo("#/components/schemas/TypeSchemaTest$MyType"));
    }

    @Test
    public void validate() {

        final TypeSchema ts = TypeSchema.typeConvert(org.hyperledger.fabric.contract.MyType.class);
        final DataTypeDefinition dtd = new DataTypeDefinitionImpl(org.hyperledger.fabric.contract.MyType.class);

        MetadataBuilder.addComponent(dtd);
        final JSONObject json = new JSONObject();
        ts.validate(json);

    }
}
