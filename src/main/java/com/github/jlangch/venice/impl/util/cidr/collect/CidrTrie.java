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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.jlangch.venice.impl.util.cidr.CIDR;


/**
 * A Cidr Trie implementation which supports lock-free concurrent reads, and allows 
 * items to be inserted to the tree <i>atomically</i> by background thread(s), 
 * without blocking reads.
 * <p/>
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
			final int ipBits = key.ipBits(); // IP4: 32, IP6: 128
			final int highestBit = ipBits-1;
			final int lowestBit = ipBits-key.getCidrRange();
			
			CidrTrieNode<V> current = root;
			String prefix = "";

			// create missing edge nodes
			for(int bit=highestBit; bit>lowestBit; bit--) {
				final boolean isLeft = !key.getLowAddressBit(bit);
				prefix = prefix + (isLeft ? "0" : "1");
				CidrTrieNode<V> child = current.getChild(isLeft);
				if (child == null) {
					child = new CidrTrieNode<>(prefix);
					current.setChild(isLeft, child);
				}
				current = child;
			}
			
			// create/update data node
			final boolean isLeft = !key.getLowAddressBit(lowestBit);
			prefix = prefix + (isLeft ? "0" : "1");
			CidrTrieNode<V> child = current.getChild(isLeft);
			if (child == null) {
				// a new date node
				child = new CidrTrieNode<>(prefix, key, value);
				current.setChild(isLeft, child);
			}
			else {
				current.setChild(isLeft, child.withData(key, value));
			}
		}
		finally {
			releaseWriteLock();
		}
	}

	public V get(final CIDR key) {
		final int ipBits = key.ipBits(); // IP4: 32, IP6: 128
		final int highestBit = ipBits-1;
		final int lowestBit = ipBits-key.getCidrRange();

		CidrTrieNode<V> current = root;
		CidrTrieNode<V> lastDataNode = null;
		
		// traverse nodes and capture the last data node
		for(int bit=highestBit; bit>=lowestBit; bit--) {
			final boolean isLeft = !key.getLowAddressBit(bit);
			CidrTrieNode<V> child = current.getChild(isLeft);
			if (child != null) {
				if (child.hasValue()) {
					lastDataNode = child;
				}
				current = child;
			}
			else {
				break;
			}
		}

		return lastDataNode == null ? null : lastDataNode.getValue();
	}


	private void acquireWriteLock() {
		writeLock.lock();
    }

	private void releaseWriteLock() {
		writeLock.unlock();
	}
	
	
	private volatile CidrTrieNode<V> root = new CidrTrieNode<>();
	
	// Write operations acquire write lock, read operations are lock-free.
	private final Lock writeLock = new ReentrantLock();
}
