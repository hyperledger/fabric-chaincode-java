/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.ledger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public class CompositeKey {

	private static final String DELIMITER = new String(Character.toChars(Character.MIN_CODE_POINT));
	static final String NAMESPACE = DELIMITER;
	private static final String INVALID_SEGMENT_CHAR = new String(Character.toChars(Character.MAX_CODE_POINT));
	private static final String INVALID_SEGMENT_PATTERN = String.format("(?:%s|%s)", INVALID_SEGMENT_CHAR, DELIMITER);

	final String objectType;
	final List<String> attributes;
	final String compositeKey;

	public CompositeKey(String objectType, String... attributes) {
		this(objectType, attributes == null ? Collections.emptyList() : Arrays.asList(attributes));
	}

	public CompositeKey(String objectType, List<String> attributes) {
		if (objectType == null) throw new NullPointerException("objectType cannot be null");
		this.objectType = objectType;
		this.attributes = attributes;
		this.compositeKey = generateCompositeKeyString(objectType, attributes);
	}

	public String getObjectType() {
		return objectType;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return compositeKey;
	}

	public static CompositeKey parseCompositeKey(String compositeKey) {
		if (compositeKey == null) return null;
		if (!compositeKey.startsWith(NAMESPACE)) throw CompositeKeyFormatException.forInputString(compositeKey, compositeKey, 0);
		// relying on the fact that NAMESPACE == DELIMETER
		final String[] segments = compositeKey.split(DELIMITER, 0);
		return new CompositeKey(segments[1], Arrays.stream(segments).skip(2).toArray(String[]::new));
	}

	/**
	 * To ensure that simple keys do not go into composite key namespace,
	 * we validate simple key to check whether the key starts with 0x00 (which
	 * is the namespace for compositeKey). This helps in avoding simple/composite
	 * key collisions.
	 *
	 * @throws CompositeKeyFormatException if First character of the key
	 */
	public static void validateSimpleKeys(String... keys) {
		for (String key : keys) {
			if(!key.isEmpty() && key.startsWith(NAMESPACE)) {
				throw CompositeKeyFormatException.forSimpleKey(key);
			}
		}
	}

	private String generateCompositeKeyString(String objectType, List<String> attributes) {

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

	private void validateCompositeKeySegment(String segment) {
		final Matcher matcher = Pattern.compile(INVALID_SEGMENT_PATTERN).matcher(segment);
		if (matcher.find()) {
			throw CompositeKeyFormatException.forInputString(segment, matcher.group(), matcher.start());
		}
	}

}
