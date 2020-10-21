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
package com.github.jlangch.venice;


/**
 * Thrown by the {@code Reader} to signal a premature end of input.
 * 
 * <p>E.g.: defining a string without closing it with the end quote before
 * the end of the input is reached
 */
public class EofException extends ParseError {

	public EofException(final String message) {
		super(message);
	}

	public EofException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EofException(final Throwable cause) {
		super(cause);
	}
	

	private static final long serialVersionUID = -23568367901801596L;
}
