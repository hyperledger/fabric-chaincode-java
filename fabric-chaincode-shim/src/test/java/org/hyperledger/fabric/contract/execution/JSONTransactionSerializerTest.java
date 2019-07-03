/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.MyType;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JSONTransactionSerializerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void toBuffer() {
		TypeRegistry tr = new TypeRegistryImpl();
		JSONTransactionSerializer serializer = new JSONTransactionSerializer(tr);

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

		MyType array[] = new MyType[2];
		array[0] = new MyType().setValue("hello");
		array[1] = new MyType().setValue("world");
		bytes = serializer.toBuffer(array, TypeSchema.typeConvert(MyType[].class));

		byte[] buffer = "[{\"value\":\"hello\"},{\"value\":\"world\"}]".getBytes(StandardCharsets.UTF_8);

		System.out.println(new String(buffer,StandardCharsets.UTF_8));
		System.out.println(new String(bytes,StandardCharsets.UTF_8));
		assertThat(bytes, equalTo(buffer));
	}

	@Test
	public void fromBufferObject() {
		byte[] buffer = "[{\"value\":\"hello\"},{\"value\":\"world\"}]".getBytes(StandardCharsets.UTF_8);

		TypeRegistry tr = new TypeRegistryImpl();
		tr.addDataType(MyType.class);

		MetadataBuilder.addComponent(tr.getDataType("MyType"));

		JSONTransactionSerializer serializer = new JSONTransactionSerializer(tr);
		TypeSchema ts = TypeSchema.typeConvert(MyType[].class);
		MyType[] o = (MyType[]) serializer.fromBuffer(buffer, ts);
		assertThat(o[0].toString(),equalTo("++++ MyType: hello"));
		assertThat(o[1].toString(),equalTo("++++ MyType: world"));

	}

	@Test
	public void toBufferPrimitive() {
		TypeRegistry tr = new TypeRegistryImpl();
		JSONTransactionSerializer serializer = new JSONTransactionSerializer(tr);

		TypeSchema ts;
		Object value;
		byte[] buffer;

		ts = TypeSchema.typeConvert(boolean.class);
		value = false;
		buffer =serializer.toBuffer(value, ts);
		assertThat(buffer,equalTo(new byte[] {102, 97, 108, 115, 101}));
		assertThat(serializer.fromBuffer(buffer, ts),equalTo(false));

		ts = TypeSchema.typeConvert(int.class);
		value = 1;
		buffer =serializer.toBuffer(value, ts);
		assertThat(buffer,equalTo(new byte[] {49}));
		assertThat(serializer.fromBuffer(buffer, ts),equalTo(1));

		ts = TypeSchema.typeConvert(long.class);
		value = 9192631770l;
		buffer =serializer.toBuffer(value, ts);
		assertThat(buffer,equalTo(new byte[] {57, 49, 57, 50, 54, 51, 49, 55, 55, 48}));
		assertThat(serializer.fromBuffer(buffer, ts),equalTo(9192631770l));

		ts = TypeSchema.typeConvert(float.class);
		float f = 3.1415927F;
		buffer =serializer.toBuffer(f, ts);
		assertThat(buffer,equalTo(new byte[] {51, 46, 49, 52, 49, 53, 57, 50, 55}));
		assertThat(serializer.fromBuffer(buffer, ts),equalTo(3.1415927F));

		ts = TypeSchema.typeConvert(double.class);
		double d = 2.7182818284590452353602874713527;
		buffer =serializer.toBuffer(d, ts);
		assertThat(buffer,equalTo(new byte[] {50, 46, 55, 49, 56, 50, 56, 49, 56, 50, 56, 52, 53, 57, 48, 52, 53}));
		assertThat(serializer.fromBuffer(buffer, ts),equalTo(2.7182818284590452353602874713527));
	}

	@Test
	public void fromBufferErrors() {
		TypeRegistry tr = new TypeRegistryImpl();
		tr.addDataType(MyType.class);
		MetadataBuilder.addComponent(tr.getDataType("MyType"));
		JSONTransactionSerializer serializer = new JSONTransactionSerializer(tr);
		TypeSchema ts = TypeSchema.typeConvert(MyType[].class);
		serializer.toBuffer(null, ts);
	}



}
