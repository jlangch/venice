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
package com.github.jlangch.venice.impl.util.markdown.renderer.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.github.jlangch.venice.impl.util.StringEscapeUtil;
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


public class HtmlRenderer {
	
	public HtmlRenderer() {
	}
	
	public String render(final Markdown md) {
		try (StringWriter sw = new StringWriter();
			 PrintWriter wr = new PrintWriter(sw)) {
			
			wr.println("<div class=\"md\">");
			wr.println();
			
			for(Block b : md.blocks().getBlocks()) {
				render(b, wr);
				wr.println();
			}
			
			wr.println("</div>");
			
			return wr.toString();
		}
		catch(IOException ex) {
			throw new RuntimeException("Failed to render markdown to HTML", ex);
		}
	}

	public void render(final Block b, final PrintWriter wr) {
		if (b.isEmpty()) {
			return ;
		}
		else if (b instanceof TextBlock) {
			render((TextBlock)b, wr);
		}
		else if (b instanceof CodeBlock) {
			render((CodeBlock)b, wr);
		}
		else if (b instanceof ListBlock) {
			render((ListBlock)b, wr);
		}
		else if (b instanceof TableBlock) {
			render((TableBlock)b, wr);
		}
		else {
			return;
		}
	}

	private void render(final TextBlock block, final PrintWriter wr) {
		wr.println("<div class=\"md-text-block\">");
		
		render(block.getChunks(), wr);
		
		wr.println("</div>");
	}

	private void render(final CodeBlock block, final PrintWriter wr) {		
		final String code = escape(String.join("\n", block.getLines()));

		wr.println("<div class=\"md-code-block\">");
		wr.print("<code>" + code + "</code>");
		wr.println("</div>");
	}
	
	private void render(final ListBlock block, final PrintWriter wr) {
		wr.println("<div class=\"md-list-block\">");	
		wr.println("<ul>");
		for(Block b : block.getItems()) {
			wr.print("<li>" );
			render(b, wr);
			wr.println("</li>" );
		}
		wr.println("</ul>");	
		wr.println("</div>");
	}
	
	private void render(final TableBlock block, final PrintWriter wr) {
		wr.println("<div class=\"md-table-block\">");
		
		wr.println("<table>");
		
		if (block.hasHeader()) {
			wr.println("<tr>");
			for(int col=0; col<block.cols(); col++) {
				final TextChunk chunk =  ((TextChunk)block.headerCell(col).get(0));
				final String header = escape(chunk.getText());
				final String format = block.format(col).name().toLowerCase();
	
				wr.println("<th class=\"md-align-" + format + "\">" + header + "</th>");
			}
			wr.println("</tr>");
		}

		for(int row=0; row<block.bodyRows(); row++) {
			wr.println("<tr>");
			for(int col=0; col<block.cols(); col++) {
				final Chunks chunks = block.bodyCell(row, col);
				final String format = block.format(row).name().toLowerCase();
	
				wr.print("<td class=\"md-align-" + format + "\">");
				
				render(chunks, wr);
				
				wr.println("</td>");
			}
			wr.println("</tr>");
		}

		wr.println("</table>");
		
		wr.println("</div>");
	}

	private void render(final Chunks chunks, final PrintWriter wr) {
		for(Chunk c : chunks.getChunks()) {
			if (c.isEmpty()) continue;
			
			if (c instanceof TextChunk) {
				render((TextChunk)c, wr);
			}
			else if (c instanceof LineBreakChunk) {
				render((LineBreakChunk)c, wr);
			}
			else if (c instanceof InlineCodeChunk) {
				render((InlineCodeChunk)c, wr);
			}
		}
	}
	
	private void render(final TextChunk chunk, final PrintWriter wr) {
		final String text = escape(chunk.getText());
		
		switch(chunk.getFormat()) {
			case NORMAL: 
				wr.println("<div class=\"md-text-normal\">" + text + "</div>");
				break;
			case ITALIC:
				wr.println("<div class=\"md-text-italic\">" + text + "</div>");
				break;
			case BOLD:
				wr.println("<div class=\"md-text-bold\">" + text + "</div>");
				break;
			case BOLD_ITALIC:
				wr.println("<div class=\"md-text-bold-italic\">" + text + "</div>");
				break;
		}
	}
	
	private String escape(final String s) {
		return StringEscapeUtil.escapeHtml(s);
	}
	
	private void render(final LineBreakChunk chunk, final PrintWriter wr) {
		wr.println("<br/>" );
	}
	
	private void render(final InlineCodeChunk chunk, final PrintWriter wr) {
		final String text = escape(chunk.getText());
		
		wr.print("<div class=\"md-inline-code\">" + text + "</div");
	}
}