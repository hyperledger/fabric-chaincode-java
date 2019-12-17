/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public final class Greeting {

    @Property()
    private String text;

    @Property()
    private int textLength;

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(final int textLength) {
        this.textLength = textLength;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(final int wordCount) {
        this.wordCount = wordCount;
    }

    private int wordCount;

    public Greeting(final String text) {
        this.text = text;
        this.textLength = text.length();
        this.wordCount = text.split(" ").length;
    }

    public static void validate(final Greeting greeting) {
        final String text = greeting.text;

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
