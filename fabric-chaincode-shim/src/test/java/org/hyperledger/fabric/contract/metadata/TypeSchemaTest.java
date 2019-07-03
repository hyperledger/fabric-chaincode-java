
/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.metadata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.DataTypeDefinitionImpl;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeSchemaTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void beforeEach() {
    }

    @Test
    public void putIfNotNull() {
        TypeSchema ts = new TypeSchema();

        ts.putIfNotNull("Key", "value");
        String nullstr = null;
        ts.putIfNotNull("Key", nullstr);

        assertThat(ts.get("Key"), equalTo("value"));
        ts.putIfNotNull("Key", "");

        assertThat(ts.get("Key"), equalTo("value"));
    }

    @Test
    public void getType() {
        TypeSchema ts = new TypeSchema();
        ts.put("type", "MyType");
        assertThat(ts.getType(), equalTo("MyType"));

        TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getType(), equalTo("MyType"));
    }

    @Test
    public void getFormat() {
        TypeSchema ts = new TypeSchema();
        ts.put("format", "MyFormat");
        assertThat(ts.getFormat(), equalTo("MyFormat"));

        TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getFormat(), equalTo("MyFormat"));
    }

    @Test
    public void getRef() {
        TypeSchema ts = new TypeSchema();
        ts.put("$ref", "#/ref/to/MyType");
        assertThat(ts.getRef(), equalTo("#/ref/to/MyType"));

        TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getRef(), equalTo("#/ref/to/MyType"));
    }

    @Test
    public void getItems() {
        TypeSchema ts1 = new TypeSchema();

        TypeSchema ts = new TypeSchema();
        ts.put("items", ts1);
        assertThat(ts.getItems(), equalTo(ts1));

        TypeSchema wrapper = new TypeSchema();
        wrapper.put("schema", ts);
        assertThat(wrapper.getItems(), equalTo(ts1));
    }

    @DataType
    class MyType {
    }

    @Test
    public void getTypeClass() {
        TypeSchema ts = new TypeSchema();

        ts.put("type", "string");
        TypeRegistry mockRegistry = new TypeRegistryImpl();
        assertThat(ts.getTypeClass(mockRegistry), equalTo(String.class));

        ts.put("type", "integer");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(int.class));

        ts.put("type", "boolean");
        assertThat(ts.getTypeClass(mockRegistry), equalTo(boolean.class));

        ts.put("type", null);
        ts.put("$ref", "#/ref/to/MyType");

        mockRegistry.addDataType(MyType.class);
        assertThat(ts.getTypeClass(mockRegistry), equalTo(MyType.class));

        TypeSchema array = new TypeSchema();
        array.put("type", "array");
        array.put("items", ts);
        assertThat(array.getTypeClass(mockRegistry), equalTo(MyType[].class));

    }

    @Test
    public void TypeConvertPrimitives() {
        TypeSchema rts;

        String[] array = new String[] {};
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
    public void TypeConvertObjects() {
        TypeSchema rts;
        rts = TypeSchema.typeConvert(String.class);
        assertThat(rts.getType(), equalTo("string"));

        String[] array = new String[] {};
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

        TypeSchema ts = TypeSchema.typeConvert(org.hyperledger.fabric.contract.MyType.class);
        DataTypeDefinition dtd = new DataTypeDefinitionImpl(org.hyperledger.fabric.contract.MyType.class);

        MetadataBuilder.addComponent(dtd);
        JSONObject json = new JSONObject();
        ts.validate(json);

    }
}
