/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Parameter;

import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;

public class ParameterDefinitionImpl implements ParameterDefinition {

	private Class<?> typeClass;
	private TypeSchema schema;
	private Parameter parameter;
	private String name;

	public ParameterDefinitionImpl(String name, Class<?> typeClass, TypeSchema schema, Parameter p) {
		this.typeClass = typeClass;
		this.schema = schema;
		this.parameter = p;
		this.name =name;
	}

	@Override
	public Class<?> getTypeClass() {
		return this.typeClass;
	}

	@Override
	public TypeSchema getSchema() {
		return this.schema;
	}

	@Override
	public Parameter getParameter() {
		return this.parameter;
	}

	@Override
	public String getName() {
		return this.name;
	}
	public String toString() {
		return this.name+"-"+this.typeClass+"-"+this.schema+"-"+this.parameter;
	}
}
