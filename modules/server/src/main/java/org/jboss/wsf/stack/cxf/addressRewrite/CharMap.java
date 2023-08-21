/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.addressRewrite;

import java.util.HashMap;
import java.util.Map;

/**
 * A map from char to char. The mappings for the most common 256 source
 * characters are simply kept in a char array. For all other more exotic source
 * characters, a proper hash map is used to store the mapping.
 */
final class CharMap {

	private final char[] map = new char[256];
	private final Map<Character, Character> extendedMap = new HashMap<Character, Character>();

	/**
	 * Creates a char map with the given source and destination characters. If
	 * the strings do not have the same length, subsequent characters in the
	 * longer string are ignored. The first mapping is defined as
	 * {@code source[0] --> destination[0]}, all other mappings in an analogous
	 * way with matching character indices in the two strings.
	 * 
	 * @param source
	 *            source characters
	 * @param destination
	 *            destination characters
	 * @throws IllegalArgumentException
	 *             if any of the destination characters is the zero character
	 */
	public CharMap(String source, String destination) {
		add(source, destination);
	}

	private void add(String source, String destination) {
		final int len = Math.min(source.length(), destination.length());
		for (int i = 0; i < len; i++) {
			add(source.charAt(i), destination.charAt(i));
		}
	}

	private void add(char source, char destination) {
		if (destination == 0) {
			throw new IllegalArgumentException("cannot map to zero character");
		}
		if (source < 256) {
			map[source] = destination;
		} else {
			extendedMap.put(source, destination);
		}
	}

	public char map(char source) {
		if (source < 256) {
			return map[source];
		}
		final Character mapped = extendedMap.get(source);
		return mapped == null ? 0 : mapped.charValue();
	}
}
