/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.DataTypeDefinition;
import org.hyperledger.fabric.contract.routing.PropertyDefinition;

public final class DataTypeDefinitionImpl implements DataTypeDefinition {

    private final Map<String, PropertyDefinition> properties = new HashMap<>();
    private final String name;
    private final String simpleName;
    private final Class<?> clazz;

    /**
     *
     * @param componentClass
     */
    public DataTypeDefinitionImpl(final Class<?> componentClass) {
        this.clazz = componentClass;
        this.name = componentClass.getName();
        this.simpleName = componentClass.getSimpleName();
        // given this class extract the property elements
        final Field[] fields = componentClass.getDeclaredFields();

        for (final Field f : fields) {
            final Property propAnnotation = f.getAnnotation(Property.class);
            if (propAnnotation != null) {
                final TypeSchema ts = TypeSchema.typeConvert(f.getType());

                // array of strings, "a","b","c","d" to become map of {a:b}, {c:d}
                final String[] userSupplied = propAnnotation.schema();
                for (int i = 0; i < userSupplied.length; i += 2) {
                    final String userKey = userSupplied[i];
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

                final PropertyDefinition propDef = new PropertyDefinitionImpl(f.getName(), f.getClass(), ts, f);
                this.properties.put(f.getName(), propDef);
            }
        }

    }

    @Override
    public Class<?> getTypeClass() {
        return this.clazz;
    }

    @Override
    public TypeSchema getSchema() {
        return TypeSchema.typeConvert(this.clazz);
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

    @Override
    public String toString() {
        return this.simpleName + " " + properties;
    }

}
