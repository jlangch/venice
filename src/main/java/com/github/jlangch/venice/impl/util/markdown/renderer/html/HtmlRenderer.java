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

import static com.github.jlangch.venice.impl.util.StringEscapeUtil.escapeHtml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
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
			
			md.blocks()
			  .getBlocks()
			  .stream()
			  .filter(b -> !b.isEmpty())
			  .forEach(b -> render(b, wr));
			
			wr.println("</div>");
			
			return sw.getBuffer().toString();
		}
		catch(IOException ex) {
			throw new RuntimeException("Failed to render markdown to HTML", ex);
		}
	}

	private void render(final Block b, final PrintWriter wr) {
		if (b instanceof TextBlock) {
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
	}

	private void render(final TextBlock block, final PrintWriter wr) {
		wr.print("<div class=\"md-text-block\">");
		render(block.getChunks(), wr);
		wr.println("</div>");
	}

	private void render(final CodeBlock block, final PrintWriter wr) {
		final String code = escapeHtml(String.join("\n", block.getLines()));

		wr.println("<div class=\"md-code-block\">");
		wr.print("<code class=\"md-code\">" + code + "</code>");
		wr.println("</div>");
	}
	
	private void render(final ListBlock block, final PrintWriter wr) {
		wr.println("<div class=\"md-list-block\">");
		
		wr.println(block.isOrdered()
					? "<ol class=\"md-list\">"
					: "<ul class=\"md-list\">");
		
		block.getItems().forEach(b -> { wr.print("<li>");
										render(b, wr);
										wr.println("</li>"); });
		
		wr.println(block.isOrdered() ? "</ol>" : "</ul>");
		wr.println("</div>");
	}
	
	private void render(final TableBlock block, final PrintWriter wr) {
		wr.println("<div class=\"md-table-block\">");
		wr.println("<table class=\"md-table\">");
		
		if (block.hasHeader()) {
			wr.println("<thead>");
			wr.println("<tr>");
			for(int col=0; col<block.cols(); col++) {
				final TextChunk chunk =  ((TextChunk)block.headerCell(col).get(0));
				final String header = escapeHtml(chunk.getText());
				final String alignClass = buildCssAlignmentClass(block.format(col));
	
				wr.println("<th class=\"" + alignClass + "\">" + header + "</th>");
			}
			wr.println("</tr>");
			wr.println("</thead>");
		}

		wr.println("<tbody>");
		for(int row=0; row<block.bodyRows(); row++) {
			wr.println("<tr>");
			for(int col=0; col<block.cols(); col++) {
				final Chunks chunks = block.bodyCell(row, col);
				final String alignClass = buildCssAlignmentClass(block.format(col));
	
				wr.print("<td class=\"" + alignClass + "\">");
				render(chunks, wr);
				wr.println("</td>");
			}
			wr.println("</tr>");
		}
		wr.println("</tbody>");

		wr.println("</table>");		
		wr.println("</div>");
	}

	private void render(final Chunks chunks, final PrintWriter wr) {
		final List<Chunk> chs = chunks.getChunks()
									  .stream()
									  .filter(c -> !c.isEmpty())
									  .collect(Collectors.toList());
		
		if (!chs.isEmpty()) {
			// first
			render(chs.get(0), wr);
			
			// rest
			chs.stream()
			   .skip(1)
			   .forEach(c -> { wr.print(" "); render(c, wr); });
		}
	}

	private void render(final Chunk c, final PrintWriter wr) {
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

	private void render(final TextChunk chunk, final PrintWriter wr) {
		final String text = escapeHtml(mapWhiteSpaces(chunk.getText()));
		final String formatClass = buildCssEmphasizeClass(chunk.getFormat());
		
		wr.print("<div class=\"" + formatClass + "\">" + text + "</div>");
	}
	
	private void render(final LineBreakChunk chunk, final PrintWriter wr) {
		wr.println("<br/>" );
	}
	
	private void render(final InlineCodeChunk chunk, final PrintWriter wr) {
		final String text = escapeHtml(chunk.getText());		
		wr.print("<div class=\"md-inline-code\">" + text + "</div>");
	}
	
	private String buildCssEmphasizeClass(final TextChunk.Format format) {
		return "md-text-" + format.name().replace('_','-').toLowerCase();
	}
	
	private String buildCssAlignmentClass(final TableBlock.Alignment alignment) {
		return "md-align-" + alignment.name().toLowerCase();
	}
	
	private String mapWhiteSpaces(final String text) {
		String s = text;
		s = StringUtil.replace(s, "\t", "\u00A0\u00A0\u00A0\u00A0", -1, false);
		s = StringUtil.replace(s, "&nbsp;", "\u00A0", -1, false);
		s = StringUtil.replace(s, "&ensp;", "\u00A0\u00A0", -1, false);
		s = StringUtil.replace(s, "&emsp;", "\u00A0\u00A0\u00A0\u00A0", -1, false);
		return s;
	}

}