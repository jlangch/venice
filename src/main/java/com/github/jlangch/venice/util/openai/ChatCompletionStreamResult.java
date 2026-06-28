/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
package com.github.jlangch.venice.util.openai;

import java.util.Objects;

public class ChatCompletionStreamResult {

	public ChatCompletionStreamResult(
			final String delta,
			final boolean terminated
	) {
		this.delta = delta;
		this.terminated = terminated;
		this.exception = null;
	}

	public ChatCompletionStreamResult(
			final Exception exception
	) {
		Objects.requireNonNull(exception);

		this.delta = null;
		this.terminated = true;
		this.exception = exception;
	}


	public String getDelta() {
		return delta;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public Exception getException() {
		return exception;
	}

	public boolean hasException() {
		return exception != null;
	}


	private final String delta;
	private final boolean terminated;
	private final Exception exception;
}
