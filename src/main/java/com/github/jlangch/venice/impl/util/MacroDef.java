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

import java.util.ArrayList;
import java.util.List;


public class MacroDef {

	public MacroDef(
			final String name, 
			final List<String> signatures, 
			final String description,
			final List<String> examples 
	) {
		this.name = name;
		this.signatures = signatures == null ? new ArrayList<>() : signatures;
		this.description = description;
		this.examples = examples == null ? new ArrayList<>() : examples;
	}

	public String getName() {
		return name;
	}

	
	public List<String> getSignatures() {
		return signatures;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getExamples() {
		return examples;
	}


	private final String name;
	private final List<String> signatures;
	private final String description;
	private final List<String> examples;
}
