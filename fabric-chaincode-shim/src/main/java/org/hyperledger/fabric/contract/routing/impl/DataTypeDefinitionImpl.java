/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;

public class DataTypeDefinitionImpl implements DataTypeDefinition {

	Map<String, PropertyDefinition> properties = new HashMap<>();
	Map<String, Field> fields = new HashMap<>();
	String name;
	String simpleName;
	Class<?> clazz;

	public DataTypeDefinitionImpl(Class<?> componentClass) {
		this.clazz = componentClass;
		this.name = componentClass.getName();
		this.simpleName = componentClass.getSimpleName();
		// given this class extract the property elements
		Field[] fields = componentClass.getDeclaredFields();

		for (Field f : fields) {
			Property propAnnotation = f.getAnnotation(Property.class);
			if (propAnnotation != null) {
				TypeSchema ts = TypeSchema.typeConvert(f.getType());

				// array of strings, "a","b","c","d" to become map of {a:b}, {c:d}
				String[] userSupplied = propAnnotation.schema();
				for (int i = 0; i < userSupplied.length; i += 2) {
					String userKey = userSupplied[i];
					Object userValue;
					switch (userKey.toLowerCase()) {
					case "title":
					case "pattern":
						userValue = userSupplied[i + 1];
						break;
					case "uniqueitems":
						userValue = Boolean.parseBoolean(userSupplied[i + 1]);
						break;
					case "required":
					case "enum":
						userValue = Stream.of(userSupplied[i + 1].split(",")).map(String::trim).toArray(String[]::new);
						break;
					default:
						userValue = Integer.parseInt(userSupplied[i + 1]);
						break;
					}
					ts.put(userKey, userValue);
				}

				PropertyDefinition propDef = new PropertyDefinitionImpl(f.getName(), f.getClass(), ts, f);
				this.properties.put(f.getName(), propDef);
			}
		}

	}

	public Class<?> getTypeClass() {
		return this.clazz;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.hyperledger.fabric.contract.routing.DataTypeDefinition#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.hyperledger.fabric.contract.routing.DataTypeDefinition#getProperties()
	 */
	@Override
	public Map<String, PropertyDefinition> getProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.hyperledger.fabric.contract.routing.DataTypeDefinition#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return simpleName;
	}

}
