/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing;

import java.lang.reflect.Parameter;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

public interface ParameterDefinition {

	Class<?> getTypeClass();

	TypeSchema getSchema();

	Parameter  getParameter();

	String getName();

}
