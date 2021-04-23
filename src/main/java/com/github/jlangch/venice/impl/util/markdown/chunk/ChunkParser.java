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

import com.github.jlangch.venice.impl.reader.CharacterReader;
import com.github.jlangch.venice.impl.util.StringUtil;


public class ChunkParser {

	public ChunkParser(final Chunks chunks) {
		this.chunksRaw = chunks;
	}
	
	
	public Chunks parse() {
		for(Chunk ch : chunksRaw.getChunks()) {
			if (ch instanceof RawChunk) {
				chunks.add(parse(((RawChunk)ch).getText()));
			}
			else {
				chunks.add(ch);
			}
		}
		
		return chunks;
	}

	private Chunks parse(final String s) {
		final Chunks chunks = new Chunks();
		
		final CharacterReader reader = new CharacterReader(s);
		
		StringBuilder sb = new StringBuilder();
		
		while(true) {
			int ch = reader.peek();
			
			if (ch == EOF) {
				chunks.add(new TextChunk(collapseWhitespaces(sb.toString())));
				break;
			}
			else if (ch == '\\') {
				reader.consume();
				ch = reader.peek();
				if (ch != EOF) {
					reader.consume();
					sb.append((char)ch);
				}
			}
			else if (ch == '*') {
				chunks.add(new TextChunk(collapseWhitespaces(sb.toString())));
				sb = new StringBuilder();

				reader.consume(); // "*" consumed
				ch = reader.peek();
				if (ch != '*') {
					chunks.add(parseEmphasizeSingle(reader));
				}
				else {
					reader.consume();  // "**" consumed
					ch = reader.peek();
					if (ch != '*') {
						chunks.add(parseEmphasizeDouble(reader));
					}
					else {
						reader.consume();  // "***" consumed
						ch = reader.peek();
						if (ch != '*') {
							chunks.add(parseEmphasizeTriple(reader));
						}
						else {
							// read all '*'
							sb.append("***");
							while(reader.peek() == '*') {
								reader.consume();
								ch = reader.peek();
								sb.append("*");
							}
						}
					}
				}
			}
			else if (ch == '`') {
				chunks.add(new TextChunk(collapseWhitespaces(sb.toString())));
				sb = new StringBuilder();

				reader.consume();
				chunks.add(parseInlineCode(reader));
			}
			else {
				reader.consume();
				sb.append((char)ch);
			}
		}		
		
		return chunks;
	}

	private Chunk parseEmphasizeSingle(final CharacterReader reader) {
		// .*....*.
		final StringBuilder sb = new StringBuilder();
		
		int last2Ch = -1;
		int last1Ch = -1;
		int ch = reader.peek();
		while(true) {
			if (ch == EOF) {
				if (last2Ch != '*' && last1Ch == '*') {
					final String chunk = collapseWhitespaces(
											StringUtil.removeEnd(
													sb.toString(), "*"));
					return new TextChunk(chunk, TextChunk.Format.ITALIC);
				}
				else {
					// premature EOF
					return new TextChunk("*" + sb.toString());
				}
			}
			else if (last2Ch != '*' && last1Ch == '*' && ch != '*') {
				final String chunk = collapseWhitespaces(
										StringUtil.removeEnd(
												sb.toString(), "*"));
				return new TextChunk(chunk, TextChunk.Format.ITALIC);
			}
			else {
				reader.consume();
				last2Ch = last1Ch;
				last1Ch = ch;
				sb.append((char)ch);
			}
			
			ch = reader.peek();
		}	
	}

	private Chunk parseEmphasizeDouble(final CharacterReader reader) {	
		// .**....**.
		final StringBuilder sb = new StringBuilder();
		
		int last3Ch = -1;
		int last2Ch = -1;
		int last1Ch = -1;
		int ch = reader.peek();
		while(true) {
			if (ch == EOF) {
				if (last3Ch != '*' && last2Ch == '*' && last1Ch == '*') {
					final String chunk = collapseWhitespaces(
											StringUtil.removeEnd(
													sb.toString(), "**"));
					return new TextChunk(chunk, TextChunk.Format.BOLD);
				}
				else {
					// premature EOF
					return new TextChunk("**" + sb.toString());
				}
			}
			else if (last3Ch != '*' && last2Ch == '*' && last1Ch == '*' && ch != '*') {
				final String chunk = collapseWhitespaces(
										StringUtil.removeEnd(
												sb.toString(), "**"));
				return new TextChunk(chunk, TextChunk.Format.BOLD);
			}
			else {
				reader.consume();
				last3Ch = last2Ch;
				last2Ch = last1Ch;
				last1Ch = ch;
				sb.append((char)ch);
			}
			
			ch = reader.peek();
		}	
	}

	private Chunk parseEmphasizeTriple(final CharacterReader reader) {	
		// .***....***.
		final StringBuilder sb = new StringBuilder();
		
		int last4Ch = -1;
		int last3Ch = -1;
		int last2Ch = -1;
		int last1Ch = -1;
		int ch = reader.peek();
		while(true) {
			if (ch == EOF) {
				if (last4Ch != '*' && last3Ch == '*'  && last2Ch == '*' && last1Ch == '*' && ch != '*') {
					final String chunk = collapseWhitespaces(
											StringUtil.removeEnd(
													sb.toString(), "***"));
					return new TextChunk(chunk, TextChunk.Format.BOLD_ITALIC);
				}
				else {
					// premature EOF
					return new TextChunk("***" + sb.toString());
				}
			}
			else if (last4Ch != '*' && last3Ch == '*'  && last2Ch == '*' && last1Ch == '*' && ch != '*') {
				final String chunk = collapseWhitespaces(
										StringUtil.removeEnd(
												sb.toString(), "***"));
				return new TextChunk(chunk, TextChunk.Format.BOLD_ITALIC);
			}
			else {
				reader.consume();
				last4Ch = last3Ch;
				last3Ch = last2Ch;
				last2Ch = last1Ch;
				last1Ch = ch;
				sb.append((char)ch);
			}
			
			ch = reader.peek();
		}	
	}

	private Chunk parseInlineCode(final CharacterReader reader) {		
		final StringBuilder sb = new StringBuilder();
		
		int ch = reader.peek();
		while(true) {
			reader.consume();

			if (ch == EOF) {
				// premature EOF
				final String chunk = collapseWhitespaces("`" + sb.toString());
				return new TextChunk(chunk);
			}
			else if (ch == '`') {
				return new InlineCodeChunk(sb.toString());
			}
			else {
				sb.append((char)ch);
			}
			
			ch = reader.peek();
		}
	}
	
	private String collapseWhitespaces(final String str) {
		return str.trim().replaceAll("\t", " ").replaceAll(" +", " ");
	}

	
	private static final int EOF = -1;
	
	private final Chunks chunksRaw;
	private final Chunks chunks = new Chunks();
}
