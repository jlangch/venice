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
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.block.Block;
import com.github.jlangch.venice.impl.util.markdown.block.CodeBlock;
import com.github.jlangch.venice.impl.util.markdown.block.ListBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TextBlock;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;
import com.github.jlangch.venice.impl.util.markdown.chunk.InlineCodeChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;


public class TextRenderer {
	
	private TextRenderer(final int width, final boolean softWrap) {
		this.width = width;
		this.softWrap = softWrap;
	}

	/**
	 * Create a text renderer that hard wraps lines at 'width' characters.
	 * 
	 * @param width The max line width. Must be greater than 0.
	 * @return The rendered markdown
	 */
	public static TextRenderer hardWrap(final int width) {
		if (width < 1) {
			throw new IllegalArgumentException("A wrap width must be positive");
		}
		
		return new TextRenderer(width, false);
	}

	/**
	 * Create a text renderer that soft wraps lines at 'width' characters.
	 * Tries to wrap at the closest whitespace character and if not possible
	 * falls back to a hard wrap.
	 * 
	 * @param width The max line width. Must be greater than 0.
	 * @return The rendered markdown
	 */
	public static TextRenderer softWrap(final int width) {
		if (width < 1) {
			throw new IllegalArgumentException("A wrap width must be positive");
		}
		
		return new TextRenderer(width, true);
	}

	public static TextRenderer nowrap() {
		return new TextRenderer(-1, false);
	}

	public String render(final Markdown md) {
		final StringBuilder sb = new StringBuilder();

		for(Block b : md.blocks().getBlocks()) {
			if (sb.length() > 0) {
				sb.append("\n\n");
			}
			
			sb.append(render(b));
		}
		
		return sb.toString();
	}

	public String render(final Block b) {
		if (b.isEmpty()) {
			return "";
		}
		else if (b instanceof TextBlock) {
			return render((TextBlock)b);
		}
		else if (b instanceof CodeBlock) {
			return render((CodeBlock)b);
		}
		else if (b instanceof ListBlock) {
			return render((ListBlock)b);
		}
		else if (b instanceof TableBlock) {
			return render((TableBlock)b);
		}
		else {
			return "";
		}
	}

	public String render(final Chunks chunks) {
		final StringBuilder sb = new StringBuilder();
		
		for(Chunk c : chunks.getChunks()) {
			if (c.isEmpty()) continue;
			
			if (sb.length() > 0) {
				sb.append(" ");
			}

			if (c instanceof TextChunk) {
				sb.append(render((TextChunk)c));
			}
			else if (c instanceof LineBreakChunk) {
				sb.append(render((LineBreakChunk)c));
			}
			else if (c instanceof InlineCodeChunk) {
				sb.append(render((InlineCodeChunk)c));
			}
		}

		return isWrap() ? wrap(sb.toString(), width) : sb.toString();
	}

	private String render(final TextBlock block) {
		return render(block.getChunks());
	}

	private String render(final CodeBlock block) {
		final StringBuilder sb = new StringBuilder();
		
		block.getLines()
			 .forEach(l -> {sb.append(l); sb.append("\n"); });

		return sb.toString();
	}
	
	private String render(final ListBlock block) {
		final StringBuilder sb = new StringBuilder();
		
		for(Block b : block.getItems()) {
			if (sb.length() > 0) {
				sb.append("\n");
			}

			sb.append(
				isWrap()
					? indent(
						wrap(render(b), width-2), 
						BULLET + " ", 
						"  ")
					: BULLET + " " + render(b));
		}

		return sb.toString();
	}
	
	private String render(final TableBlock block) {
		return new TextTableRendrer(block, width).render();
	}
	
	private String render(final TextChunk chunk) {
		return chunk.getText();
	}
	
	private String render(final LineBreakChunk chunk) {
		return "\n";
	}
	
	private String render(final InlineCodeChunk chunk) {
		return chunk.getText();
	}
	
	private String wrap(final String text, final int maxWidth) {
		return String.join(
				"\n", 
				softWrap 
					? LineWrap.softWrap(text, maxWidth)
					: LineWrap.hardWrap(text, maxWidth));
	}

	private String indent(
			final String text, 
			final String firstLineIndent,
			final String indent
	) {
		final List<String> lines = new ArrayList<>();
		
		final LineReader reader = new LineReader(text);

		if (!reader.eof()) {
			lines.add(firstLineIndent + reader.peek().trim());
			reader.consume();
	
			while(!reader.eof()) {
				lines.add(indent + reader.peek().trim());
				reader.consume();
			}
		}
		
		return String.join("\n", lines);
	}
	
	private boolean isWrap() {
		return width > 0;
	}

	
	private static final char BULLET = 'o';
	
	private final int width;
	private final boolean softWrap;
}
