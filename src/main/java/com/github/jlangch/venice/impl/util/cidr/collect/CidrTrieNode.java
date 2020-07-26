/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.util.cidr.collect;

import java.util.concurrent.atomic.AtomicReferenceArray;

import com.github.jlangch.venice.impl.util.cidr.CIDR;


public class CidrTrieNode<V> {

	public CidrTrieNode() {
		this(null, null, null, null);
	}

	public CidrTrieNode(final CIDR key, final V value) {
		this(key, value, null, null);
	}

	public CidrTrieNode(
			final CIDR key, 
			final V value, 
			final CidrTrieNode<V> left, 
			final CidrTrieNode<V> right
	) {
		this.key = key;
		this.value = value;
		this.children.set(0, left);
		this.children.set(1, right);
	}

	
	public CIDR getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public CidrTrieNode<V> getChild(final boolean left) {
		return children.get(left ? 0 : 1);
	}

	public void setChild(final boolean left, final CidrTrieNode<V> child) {
		children.set(left ? 0 : 1, child);
	}

	public CidrTrieNode<V> withData(final CIDR key, final V value) {
		return new CidrTrieNode<>(
						key, 
						value, 
						children.get(0), 
						children.get(1));
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		
		sb.append(key == null ? "<null>" : key.getNotation())
		  .append(", [")
		  .append(children.get(0) != null ? "+" : "-")
		  .append(",")
		  .append(children.get(1) != null ? "+" : "-")
		  .append("]");
		
		return sb.toString();
	}
	
	
	private final CIDR key;
	private final V value;
	private final AtomicReferenceArray<CidrTrieNode<V>> children = new AtomicReferenceArray<>(2);
}
