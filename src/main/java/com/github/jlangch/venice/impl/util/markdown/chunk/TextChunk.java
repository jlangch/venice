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
package com.github.jlangch.venice.impl.util.markdown.chunk;


public class TextChunk implements Chunk {

	public TextChunk() {
		this("", Format.NORMAL);
	}

	public TextChunk(final String text) {
		this(text, Format.NORMAL);
	}

	public TextChunk(final String text, final Format format) {
		this.text = text == null ? "" : text;
		this.format = format == null ? Format.NORMAL : format;
	}

	@Override
	public boolean isEmpty() {
		return text.isEmpty();
	}
	
	public String getText() {
		return text;
	}

	public Format getFormat() {
		return format;
	}


	public static enum Format { NORMAL, ITALIC, BOLD, BOLD_ITALIC };
	
	private final String text;
	private final Format format;
}
