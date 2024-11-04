/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class AllTypesAsset {

    @Property
    private byte theByte = 125;

    @Property
    private short theShort = 30000;

    @Property
    private int theInt = 1234577123;

    @Property
    private long theLong = 12384233333123L;

    @Property
    private float theFloat = 3.1415926535_8979323846_2643383279_5028841971_6939937510_5820974944_5923078164f;

    @Property
    private double theDouble =
            3.1415926535_8979323846_2643383279_5028841971_6939937510_5820974944_5923078164_0628620899_8628034825_3421170679d;

    @Property
    private boolean theBoolean = false;

    @Property
    private char theChar = 'a';

    @Property
    private String theString = "Hello World";

    @Property
    private MyType theCustomObject = new MyType().setValue("Hello World");

    public byte getTheByte() {
        return theByte;
    }

    public void setTheByte(final byte aByte) {
        this.theByte = aByte;
    }

    public short getTheShort() {
        return theShort;
    }

    public void setTheShort(final short aShort) {
        this.theShort = aShort;
    }

    public int getTheInt() {
        return theInt;
    }

    public void setTheInt(final int aInt) {
        this.theInt = aInt;
    }

    public long getTheLong() {
        return theLong;
    }

    public void setTheLong(final long aLong) {
        this.theLong = aLong;
    }

    public float getTheFloat() {
        return theFloat;
    }

    public void setTheFloat(final float aFloat) {
        this.theFloat = aFloat;
    }

    public double getTheDouble() {
        return theDouble;
    }

    public void setTheDouble(final double aDouble) {
        this.theDouble = aDouble;
    }

    public boolean isTheBoolean() {
        return theBoolean;
    }

    public void setBoolean(final boolean aBoolean) {
        this.theBoolean = aBoolean;
    }

    public char getTheChar() {
        return theChar;
    }

    public void setTheChar(final char aChar) {
        this.theChar = aChar;
    }

    public String getTheString() {
        return theString;
    }

    public void setString(final String aString) {
        this.theString = aString;
    }

    public MyType getTheCustomObject() {
        return theCustomObject;
    }

    public void setTheCustomObject(final MyType customObject) {
        this.theCustomObject = customObject;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AllTypesAsset)) {
            return false;
        }

        AllTypesAsset obj = (AllTypesAsset) other;
        return theByte == obj.getTheByte()
                && theShort == obj.getTheShort()
                && theInt == obj.getTheInt()
                && theLong == obj.getTheLong()
                && theFloat == obj.getTheFloat()
                && theDouble == obj.getTheDouble()
                && theBoolean == obj.isTheBoolean()
                && theString.equals(obj.getTheString());
    }

    @Override
    public String toString() {
        return System.lineSeparator() + "byte=" + theByte + System.lineSeparator() + "short="
                + theShort + System.lineSeparator() + "int="
                + theInt + System.lineSeparator() + "long="
                + theLong + System.lineSeparator() + "float="
                + theFloat + System.lineSeparator() + "double="
                + theDouble + System.lineSeparator() + "boolean="
                + theBoolean + System.lineSeparator() + "char="
                + theChar + System.lineSeparator() + "String="
                + theString + System.lineSeparator() + "Mytype="
                + theCustomObject + System.lineSeparator();
    }
}
