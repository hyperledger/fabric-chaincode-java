/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;


public class DataTypeDefinitionImpl implements DataTypeDefinition {

	Map<String, Object> properties = new HashMap<>();
	String name;
	String simpleName;

	public DataTypeDefinitionImpl(Class<?> componentClass) {
		this.name = componentClass.getName();
		this.simpleName = componentClass.getSimpleName();
        // given this class extract the property elements
        Field[] fields = componentClass.getDeclaredFields();

        for (Field f : fields) {
            Property propAnnotation = f.getAnnotation(Property.class);
            if (propAnnotation != null) {
                properties.put(f.getName(), MetadataBuilder.propertySchema(f.getType()));
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.hyperledger.fabric.contract.routing.DataTypeDefinition#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.hyperledger.fabric.contract.routing.DataTypeDefinition#getProperties()
	 */
	@Override
	public Object getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.hyperledger.fabric.contract.routing.DataTypeDefinition#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return simpleName;
	}

}
