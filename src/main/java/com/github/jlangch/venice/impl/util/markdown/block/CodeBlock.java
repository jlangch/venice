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
package com.github.jlangch.venice.impl.util.markdown.block;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.util.StringUtil;


public class CodeBlock implements Block {

	public CodeBlock() {
		this(null);
	}

	public CodeBlock(final String language) {
		this.language = StringUtil.isEmpty(language) ? "text" : language;
	}


	public void addLine(final String line) {
		lines.add(StringUtil.trimToEmpty(line));
	}
	
	public List<String> getLines() {
		return lines;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public boolean isEmpty() {
		return lines.isEmpty();
	}
	
	
	private final String language;
	private List<String> lines = new ArrayList<>();
}
