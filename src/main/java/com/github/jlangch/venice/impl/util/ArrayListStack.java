/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

public final class ArrayListStack<T> extends ArrayList<T> {

	public ArrayListStack(final int initialCapacity) {
		super(initialCapacity);
	}

	public ArrayListStack() {
		this(10);
	}

	public ArrayListStack(final Collection<T> collection) {
		super(collection);
	}

	public final void push(final T item) {
		add(item);
	}

	public final T pop() {
		final T top = peek();
		remove(size() - 1);
		return top;
	}

	public final T peek() {
		int size = size();
		if (size == 0) {
			throw new EmptyStackException();
		}
		return get(size - 1);
	}

	public final boolean empty() {
		return size() == 0;
	}

	private static final long serialVersionUID = 1L;
}
