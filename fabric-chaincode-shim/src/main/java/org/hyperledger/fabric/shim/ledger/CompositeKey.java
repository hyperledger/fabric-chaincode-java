/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.ledger;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompositeKey {

    private static final String DELIMITER = new String(Character.toChars(Character.MIN_CODE_POINT));

    private static final String INVALID_SEGMENT_CHAR = new String(Character.toChars(Character.MAX_CODE_POINT));
    private static final String INVALID_SEGMENT_PATTERN = String.format("(?:%s|%s)", INVALID_SEGMENT_CHAR, DELIMITER);

    /**
    *
    */
    public static final String NAMESPACE = DELIMITER;

    private final String objectType;
    private final List<String> attributes;
    private final String compositeKey;

    /**
     *
     * @param objectType
     * @param attributes
     */
    public CompositeKey(final String objectType, final String... attributes) {
        this(objectType, attributes == null ? Collections.emptyList() : Arrays.asList(attributes));
    }

    /**
     *
     * @param objectType
     * @param attributes
     */
    public CompositeKey(final String objectType, final List<String> attributes) {
        if (objectType == null) {
            throw new NullPointerException("objectType cannot be null");
        }
        this.objectType = objectType;
        this.attributes = attributes;
        this.compositeKey = generateCompositeKeyString(objectType, attributes);
    }

    /**
     *
     * @return object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     *
     * @return List of string arguments
     */
    public List<String> getAttributes() {
        return attributes;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return compositeKey;
    }

    /**
     *
     * @param compositeKey
     * @return Composite Key
     */
    public static CompositeKey parseCompositeKey(final String compositeKey) {
        if (compositeKey == null) {
            return null;
        }
        if (!compositeKey.startsWith(NAMESPACE)) {
            throw CompositeKeyFormatException.forInputString(compositeKey, compositeKey, 0);
        }
        // relying on the fact that NAMESPACE == DELIMETER
        final String[] segments = compositeKey.split(DELIMITER, 0);
        return new CompositeKey(segments[1], Arrays.stream(segments).skip(2).toArray(String[]::new));
    }

    /**
     * To ensure that simple keys do not go into composite key namespace, we
     * validate simple key to check whether the key starts with 0x00 (which is the
     * namespace for compositeKey). This helps in avoiding simple/composite key
     * collisions.
     *
     * @param keys the list of simple keys
     * @throws CompositeKeyFormatException if First character of the key
     */
    public static void validateSimpleKeys(final String... keys) {
        for (final String key : keys) {
            if (!key.isEmpty() && key.startsWith(NAMESPACE)) {
                throw CompositeKeyFormatException.forSimpleKey(key);
            }
        }
    }

    private String generateCompositeKeyString(final String objectType, final List<String> attributes) {

        // object type must be a valid composite key segment
        validateCompositeKeySegment(objectType);

        if (attributes == null || attributes.isEmpty()) {
            return NAMESPACE + objectType + DELIMITER;
        }
        // the attributes must be valid composite key segments
        attributes.forEach(this::validateCompositeKeySegment);

        // return NAMESPACE + objectType + DELIMITER + (attribute + DELIMITER)*
        return attributes.stream().collect(joining(DELIMITER, NAMESPACE + objectType + DELIMITER, DELIMITER));

    }

    private void validateCompositeKeySegment(final String segment) {
        final Matcher matcher = Pattern.compile(INVALID_SEGMENT_PATTERN).matcher(segment);
        if (matcher.find()) {
            throw CompositeKeyFormatException.forInputString(segment, matcher.group(), matcher.start());
        }
    }

}
