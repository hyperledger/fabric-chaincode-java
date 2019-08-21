/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class Greeting {

    @Property()
    private String text;

    @Property()
    private int textLength;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    private int wordCount;

    public Greeting(String text) {
        this.text = text;
        this.textLength = text.length();
        this.wordCount = text.split(" ").length;
    }

    public static void validate(Greeting greeting) {
        String text = greeting.text;

        if (text.length() != greeting.textLength) {
            throw new Error("Length incorrectly set");
        }

        if (text.split(" ").length != greeting.wordCount) {
            throw new Error("Word count incorrectly set");
        }

    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

}
