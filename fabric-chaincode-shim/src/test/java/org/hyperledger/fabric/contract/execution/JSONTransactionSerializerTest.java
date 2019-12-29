/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.AllTypesAsset;
import org.hyperledger.fabric.contract.MyType;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

public class JSONTransactionSerializerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void toBuffer() {
        final TypeRegistry tr = TypeRegistry.getRegistry();

        tr.addDataType(MyType.class);

        MetadataBuilder.addComponent(tr.getDataType("MyType"));
        final JSONTransactionSerializer serializer = new JSONTransactionSerializer();

        byte[] bytes = serializer.toBuffer("hello world", TypeSchema.typeConvert(String.class));
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("hello world"));

        bytes = serializer.toBuffer(42, TypeSchema.typeConvert(Integer.class));
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("42"));

        bytes = serializer.toBuffer(true, TypeSchema.typeConvert(Boolean.class));
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("true"));

        bytes = serializer.toBuffer(new MyType(), TypeSchema.typeConvert(MyType.class));
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("{}"));

        bytes = serializer.toBuffer(new MyType().setValue("Hello"), TypeSchema.typeConvert(MyType.class));
        assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("{\"value\":\"Hello\"}"));

        final MyType[] array = new MyType[2];
        array[0] = new MyType().setValue("hello");
        array[1] = new MyType().setValue("world");
        bytes = serializer.toBuffer(array, TypeSchema.typeConvert(MyType[].class));

        final byte[] buffer = "[{\"value\":\"hello\"},{\"value\":\"world\"}]".getBytes(StandardCharsets.UTF_8);

        System.out.println(new String(buffer, StandardCharsets.UTF_8));
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        assertThat(bytes, equalTo(buffer));
    }

    @Nested
    @DisplayName("Complex Data types")
    class ComplexDataTypes {

        @Test
        public void alltypes() {
            final TypeRegistry tr = TypeRegistry.getRegistry();
            tr.addDataType(AllTypesAsset.class);
            tr.addDataType(MyType.class);
            MetadataBuilder.addComponent(tr.getDataType("MyType"));
            MetadataBuilder.addComponent(tr.getDataType("AllTypesAsset"));

            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final AllTypesAsset all = new AllTypesAsset();

            final TypeSchema ts = TypeSchema.typeConvert(AllTypesAsset.class);
            System.out.println("TS = " + ts);
            final byte[] bytes = serializer.toBuffer(all, ts);
            System.out.println("Data as toBuffer-ed " + new String(bytes, StandardCharsets.UTF_8));

            final AllTypesAsset returned = (AllTypesAsset) serializer.fromBuffer(bytes, ts);
            System.out.println("Start object = " + all);
            System.out.println("Returned object = " + returned);
            assertTrue(all.equals(returned));
        }
    }

    @Nested
    @DisplayName("Primitive Arrays")
    class PrimitiveArrays {
        @Test
        public void ints() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            // convert array of primitive
            final int[] intarray = new int[] {42, 83};
            final byte[] bytes = serializer.toBuffer(intarray, TypeSchema.typeConvert(int[].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[42,83]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(int[].class));
            assertThat(returned, equalTo(intarray));
        }

        @Test
        public void bytes() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            // convert array of primitive
            final byte[] array = new byte[] {42, 83};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(byte[].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[42,83]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(byte[].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void floats() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            // convert array of primitive
            final float[] array = new float[] {42.5F, 83.5F};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(float[].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[42.5,83.5]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(float[].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void booleans() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            // convert array of primitive
            final boolean[] array = new boolean[] {true, false, true};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(boolean[].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[true,false,true]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(boolean[].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void chars() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            // convert array of primitive
            final char[] array = new char[] {'a', 'b', 'c'};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(char[].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[\"a\",\"b\",\"c\"]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(char[].class));
            assertThat(returned, equalTo(array));
        }
    }

    @Nested
    @DisplayName("Nested Arrays")
    class NestedArrays {
        @Test
        public void ints() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final int[][] array = new int[][] {{42, 83}, {83, 42}};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(int[][].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[[42,83],[83,42]]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(int[][].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void longs() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final long[][] array = new long[][] {{42L, 83L}, {83L, 42L}};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(long[][].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[[42,83],[83,42]]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(long[][].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void doubles() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final double[][] array = new double[][] {{42.42d, 83.83d}, {83.23d, 42.33d}};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(double[][].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[[42.42,83.83],[83.23,42.33]]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(double[][].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void bytes() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final byte[][] array = new byte[][] {{42, 83}, {83, 42}};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(byte[][].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[[42,83],[83,42]]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(byte[][].class));
            assertThat(returned, equalTo(array));
        }

        @Test
        public void shorts() {
            final JSONTransactionSerializer serializer = new JSONTransactionSerializer();
            final short[][] array = new short[][] {{42, 83}, {83, 42}};
            final byte[] bytes = serializer.toBuffer(array, TypeSchema.typeConvert(short[][].class));
            assertThat(new String(bytes, StandardCharsets.UTF_8), equalTo("[[42,83],[83,42]]"));

            final Object returned = serializer.fromBuffer(bytes, TypeSchema.typeConvert(short[][].class));
            assertThat(returned, equalTo(array));
        }
    }

    @Test
    public void fromBufferObject() {
        final byte[] buffer = "[{\"value\":\"hello\"},{\"value\":\"world\"}]".getBytes(StandardCharsets.UTF_8);

        final TypeRegistry tr = TypeRegistry.getRegistry();
        tr.addDataType(MyType.class);

        MetadataBuilder.addComponent(tr.getDataType("MyType"));

        final JSONTransactionSerializer serializer = new JSONTransactionSerializer();

        final TypeSchema ts = TypeSchema.typeConvert(MyType[].class);
        final MyType[] o = (MyType[]) serializer.fromBuffer(buffer, ts);
        assertThat(o[0].toString(), equalTo("++++ MyType: hello"));
        assertThat(o[1].toString(), equalTo("++++ MyType: world"));

    }

    @Test
    public void toBufferPrimitive() {
        final TypeRegistry tr = TypeRegistry.getRegistry();
        final JSONTransactionSerializer serializer = new JSONTransactionSerializer();

        TypeSchema ts;
        Object value;
        byte[] buffer;

        ts = TypeSchema.typeConvert(boolean.class);
        value = false;
        buffer = serializer.toBuffer(value, ts);
        assertThat(buffer, equalTo(new byte[] {102, 97, 108, 115, 101}));
        assertThat(serializer.fromBuffer(buffer, ts), equalTo(false));

        ts = TypeSchema.typeConvert(int.class);
        value = 1;
        buffer = serializer.toBuffer(value, ts);

        assertThat(buffer, equalTo(new byte[] {49}));
        assertThat(serializer.fromBuffer(buffer, ts), equalTo(1));

        ts = TypeSchema.typeConvert(long.class);
        value = 9192631770L;
        buffer = serializer.toBuffer(value, ts);
        assertThat(buffer, equalTo(new byte[] {57, 49, 57, 50, 54, 51, 49, 55, 55, 48}));
        assertThat(serializer.fromBuffer(buffer, ts), equalTo(9192631770L));

        ts = TypeSchema.typeConvert(float.class);
        final float f = 3.1415927F;
        buffer = serializer.toBuffer(f, ts);
        assertThat(buffer, equalTo(new byte[] {51, 46, 49, 52, 49, 53, 57, 50, 55}));
        assertThat(serializer.fromBuffer(buffer, ts), equalTo(3.1415927F));

        ts = TypeSchema.typeConvert(double.class);
        final double d = 2.7182818284590452353602874713527;
        buffer = serializer.toBuffer(d, ts);
        assertThat(buffer, equalTo(new byte[] {50, 46, 55, 49, 56, 50, 56, 49, 56, 50, 56, 52, 53, 57, 48, 52, 53}));
        assertThat(serializer.fromBuffer(buffer, ts), equalTo(2.7182818284590452353602874713527));
    }

    @Test
    public void fromBufferErrors() {
        final TypeRegistry tr = new TypeRegistryImpl();
        tr.addDataType(MyType.class);
        MetadataBuilder.addComponent(tr.getDataType("MyType"));
        final JSONTransactionSerializer serializer = new JSONTransactionSerializer();

        final TypeSchema ts = TypeSchema.typeConvert(MyType[].class);
        serializer.toBuffer(null, ts);
    }

    class MyTestObject {

    }

}
