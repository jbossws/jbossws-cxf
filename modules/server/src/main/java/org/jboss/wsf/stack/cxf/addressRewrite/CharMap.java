/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
