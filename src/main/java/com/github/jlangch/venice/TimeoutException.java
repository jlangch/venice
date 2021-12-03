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
package com.github.jlangch.venice;


/**
 * Exception thrown when an operation times out. 
 */
public class TimeoutException extends VncException {

	public TimeoutException() {
	}
	
	public TimeoutException(final String message) {
		super(message);
	}

	public TimeoutException(final Throwable cause) {
		super(cause);
	}

	public TimeoutException(final String message, final Throwable cause) {
		super(message, cause);
	}


	private static final long serialVersionUID = 1349237272157335345L;
}
