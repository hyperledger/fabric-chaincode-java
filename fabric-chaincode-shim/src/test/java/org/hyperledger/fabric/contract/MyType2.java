/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public final class MyType2 {

    @Property()
    private String value;

    @Property(schema = {"title", "MrProperty", "Pattern", "[a-z]", "uniqueItems", "false", "required", "true,false", "enum", "a,bee,cee,dee", "minimum", "42"})
    private String constrainedValue;

    public MyType2 setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "++++ MyType: " + value;
    }
}
