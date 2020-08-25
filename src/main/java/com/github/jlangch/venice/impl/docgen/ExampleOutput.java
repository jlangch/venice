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
package com.github.jlangch.venice.impl.docgen;


public class ExampleOutput {

	public ExampleOutput(
			final long idx,
			final String name,
			final String example
	) {
		this.idx = idx;
		this.name = name;
		this.example = example;
		this.stdout = null;
		this.stderr = null;
		this.result = null;
		this.ex = null;
	}

	public ExampleOutput(
			final long idx,
			final String name,
			final String example,
			final String stdout,
			final String stderr,
			final String result
	) {
		this.idx = idx;
		this.name = name;
		this.example = example;
		this.stdout = stdout;
		this.stderr = stderr;
		this.result = result;
		this.ex = null;
	}
	
	public ExampleOutput(
			final long idx,
			final String name,
			final String example,
			final String stdout,
			final String stderr,
			final RuntimeException ex
	) {
		this.idx = idx;
		this.name = name;
		this.example = example;
		this.stdout = stdout;
		this.stderr = stderr;
		this.result = null;
		this.ex = ex;
	}

	
	
	public long getIdx() {
		return idx;
	}

	public String getName() {
		return name;
	}

	public String getExample() {
		return example;
	}

	public String getResult() {
		return result;
	}

	public String getStdout() {
		return stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public RuntimeException getEx() {
		return ex;
	}

	public String getExString() {
		return ex != null
				? String.format(
						"%s: %s", 
						ex.getClass().getSimpleName(),
						ex.getMessage())
				: null;
	}

	public String render() {
		final StringBuilder sb = new StringBuilder();

		sb.append(example).append("\n");
		if (stdout != null && !stdout.isEmpty()) {
			sb.append(stdout);
			if (!stdout.endsWith("\n")) {
				sb.append("\n");
			}
		}
		if (stderr != null && !stderr.isEmpty()) {
			sb.append(stderr);
			if (!stderr.endsWith("\n")) {
				sb.append("\n");
			}
		}

		if (result != null) {
			sb.append("=> ")
			  .append(result);
		}
		
		if (ex != null) {
			sb.append("=> ")
			  .append(getExString());
		}
		
		return sb.toString();
	}

	public boolean isFirst() {
		return idx == 0L;
	}
	
	
	private final long idx;
	private final String name;
	private final String example;
	private final String result;
	private final String stdout;
	private final String stderr;
	private final RuntimeException ex;
}
