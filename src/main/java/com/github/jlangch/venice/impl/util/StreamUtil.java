/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class StreamUtil {

	/**
	 * Returns a <tt>Stream</tt> from an <tt>Iterable</tt>
	 * 
	 * @param <T> the type of the stream elements
	 * @param in An iterable
	 * @return A stream
	 */
	public static <T> Stream<T> stream(final Iterable<T> in) {
	    return StreamSupport.stream(in.spliterator(), false);
	}

	/**
	 * Returns a <tt>Stream</tt> from an <tt>Enumeration</tt>
	 * 
	 * @param <T> the type of the stream elements
	 * @param e An iterable
	 * @return A stream
	 */
	public static <T> Stream<T> stream(final Enumeration<T> e) {
	    return StreamSupport.stream(
	        Spliterators.spliteratorUnknownSize(
	            new Iterator<T>() {
	                public T next() {
	                    return e.nextElement();
	                }
	                public boolean hasNext() {
	                    return e.hasMoreElements();
	                }
	            },
	            Spliterator.ORDERED), 
	        false);
	}
 
}
