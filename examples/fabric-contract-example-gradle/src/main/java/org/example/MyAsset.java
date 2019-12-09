/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class MyAsset {

    @Property()
    private String value;

    public MyAsset(){
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static MyAsset fromJSONString(String json) {
        String value = new JSONObject(json).getString("value");
        MyAsset asset = new MyAsset();
        asset.setValue(value);
        return asset;
    }
}
