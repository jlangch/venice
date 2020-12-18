/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.jlangch.venice.impl.util.cidr.CIDR;


/**
 * A Cidr Trie implementation which supports lock-free concurrent reads, and allows 
 * items to be inserted to the tree <i>atomically</i> by background thread(s), 
 * without blocking reads.
 * 
 * <p>
 * Unlike reads, writes lock the tree (locking out other writing threads only; 
 * reading threads are never blocked).
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Trie">Trie</a>
 */
public class CidrTrie<V> {

	public CidrTrie() {
	}
	
	public void insert(final CIDR key, final V value) {
		acquireWriteLock();
		
		try {
			final int ipBits = key.isIP4() ? 32 : 128;
			final int highestBit = ipBits-1;
			final int lowestBit = ipBits-key.getRange();
			
			CidrTrieNode<V> current = root;

			// create missing edge nodes
			for(int bit=highestBit; bit>lowestBit; bit--) {
				final boolean isLeft = !key.getLowAddressBit(bit);
				CidrTrieNode<V> child = current.getChild(isLeft);
				if (child == null) {
					child = new CidrTrieNode<>();
					current.setChild(isLeft, child);
				}
				current = child;
			}
			
			// create or update the value node
			final boolean isLeft = !key.getLowAddressBit(lowestBit);
			CidrTrieNode<V> child = current.getChild(isLeft);
			if (child == null) {
				// a new value node
				child = new CidrTrieNode<>(key, value);
				current.setChild(isLeft, child);
				
				if (value != null) {
					size.incrementAndGet();
				}
			}
			else {
				// update value node
				current.setChild(isLeft, child.withData(key, value));
			}
		}
		finally {
			releaseWriteLock();
		}
	}

	public V getValue(final String ipAddr) {
		final CidrTrieNode<V> node = getNode(CIDR.parse(ipAddr));	
		return node == null ? null : node.getValue();
	}
	
	public V getValue(final CIDR key) {
		final CidrTrieNode<V> node = getNode(key);	
		return node == null ? null : node.getValue();
	}

	public CIDR getCIDR(final String ipAddr) {
		final CIDR key = CIDR.parse(ipAddr);
		
		final CidrTrieNode<V> node = getNode(key);	
		return node == null ? null : node.getKey();
	}

	public void clear() {
		acquireWriteLock();
		
		try {
			root = new CidrTrieNode<>();
			size.set(0);
		}
		finally {
			releaseWriteLock();
		}
	}

	public int size() {
		return size.get();
	}
	
	
	private CidrTrieNode<V> getNode(final CIDR key) {
		final int ipBits = key.isIP4() ? 32 : 128;
		final int highestBit = ipBits-1;
		final int lowestBit = ipBits-key.getRange();

		CidrTrieNode<V> current = root;
		CidrTrieNode<V> lastValueNode = null;
		
		// traverse nodes and capture the last value node
		for(int bit=highestBit; bit>=lowestBit; bit--) {
			final boolean isLeft = !key.getLowAddressBit(bit);
			final CidrTrieNode<V> child = current.getChild(isLeft);
			if (child != null) {
				if (child.hasValue()) {
					lastValueNode = child;
				}
				current = child;
			}
			else {
				break;
			}
		}

		return lastValueNode;
	}
	
	private void acquireWriteLock() {
		writeLock.lock();
    }

	private void releaseWriteLock() {
		writeLock.unlock();
	}
	
	
	private volatile CidrTrieNode<V> root = new CidrTrieNode<>();
	
	private final AtomicInteger size = new AtomicInteger(0);
	
	// Write operations acquire write lock, read operations are lock-free.
	private final Lock writeLock = new ReentrantLock();
}
