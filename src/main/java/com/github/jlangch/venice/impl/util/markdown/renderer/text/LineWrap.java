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
package com.github.jlangch.venice.impl.util.markdown.renderer.text;

import java.util.ArrayList;
import java.util.List;

import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.StringUtil;


public class LineWrap {

	public static List<String> wrap(final String text, final int maxWidth) {
		if (maxWidth < 1) {
			throw new IllegalArgumentException("A maxWidth must be a positive number!");
		}
		
		if (StringUtil.isBlank(text)) {
			return new ArrayList<>();
		}
		
		final List<String> lines = new ArrayList<>();

		final LineReader reader = new LineReader(text);
		while(!reader.eof()) {
			lines.addAll(
					wrapLine(
						reader.peek().trim(), 
						maxWidth));		
			reader.consume();
		}
		
		return lines;
	}

	private static List<String> wrapLine(final String line, final int maxWidth) {
		final List<String> lines = new ArrayList<>();

		if (line.length() <= maxWidth) {
			lines.add(line); 
		}
		else {
			final int minWidth = maxWidth / 2;
			
			String rest = line;
			
			while (rest.length() > maxWidth) {
				int pos = maxWidth;
				
				while(pos >= minWidth && rest.charAt(pos) != ' ') pos--;
				
				if (pos >= minWidth) {
					// soft wrap					
					final String part = rest.substring(0, pos).trim();
					rest = rest.substring(pos).trim();
					lines.add(part); 
				}
				else {
					// hard wrap
					final String part = rest.substring(0, maxWidth).trim();
					rest = rest.substring(maxWidth).trim();
					lines.add(part); 
				}
			}
			
			if (!rest.isEmpty()) {
				lines.add(rest);
			}
		}
		
		return lines;
	}

}
