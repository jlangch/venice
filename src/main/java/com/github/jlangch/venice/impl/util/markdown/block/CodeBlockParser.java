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

import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.StringUtil;


public class CodeBlockParser {

	public CodeBlockParser(final LineReader reader) {
		this.reader = reader;
	}
	
	public CodeBlock parse() {
		if (reader.eof()) {
			return new CodeBlock();
		}

		String line = reader.peek();
		
		if (CodeBlockParser.isBlockStart(line)) {
			reader.consume();
		
			final int indent = parseIndent(line);
			final String language = parseLanguage(line);
			
			final CodeBlock block = new CodeBlock(language);
			
			while(!reader.eof()) {
				line = reader.peek();
				reader.consume();
				
				if (isFence(line)) {
					break;
				}
				else {
					block.addLine(removeIndent(StringUtil.trimRight(line), indent));
				}
			}
			
			block.parseChunks();
			return block;
		}
		else {
			return new CodeBlock();
		}
	}
	
	public static boolean isBlockStart(final String line) {
		return isFence(line);
	}
	
	private static boolean isFence(final String line) {
		return line.matches(" *```.*");
	}
		
	private static String parseLanguage(final String line) {
		return StringUtil.removeStart(line.trim(), "```").trim();
	}

	private static int parseIndent(final String line) {
		return StringUtil.indexNotOf(line, " ", 0);
	}

	private static String removeIndent(final String line, final int indent) {
		if (indent == 0 ) {
			return line;
		}
		else {
			final int lineIndent = Math.min(parseIndent(line), indent);
			
			return lineIndent == 0 ? line : line.substring(lineIndent);
		}
	}

	
	private final LineReader reader;
}
