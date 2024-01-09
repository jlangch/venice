/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.block.Block;
import com.github.jlangch.venice.impl.util.markdown.block.CodeBlock;
import com.github.jlangch.venice.impl.util.markdown.block.ListBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.HorzAlignment;
import com.github.jlangch.venice.impl.util.markdown.block.TableColFmt.Width;
import com.github.jlangch.venice.impl.util.markdown.block.TextBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TitleBlock;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;
import com.github.jlangch.venice.impl.util.markdown.chunk.InlineCodeChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.UrlChunk;
import com.github.jlangch.venice.impl.util.markdown.renderer.text.TextTableUtil;


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
        if (b instanceof TitleBlock) {
            render((TitleBlock)b, wr);
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
    }

    private void render(final TextBlock block, final PrintWriter wr) {
        wr.print("<div class=\"md-text-block\">");
        render(block.getChunks(), wr);
        wr.println("</div>");
    }

    private void render(final TitleBlock block, final PrintWriter wr) {
        switch(block.getLevel()) {
            case 1:  wr.println("<div class=\"md-h1\">" + escapeHtml(block.getText()) + "</div>"); break;
            case 2:  wr.println("<div class=\"md-h2\">" + escapeHtml(block.getText()) + "</div>"); break;
            case 3:  wr.println("<div class=\"md-h3\">" + escapeHtml(block.getText()) + "</div>"); break;
            case 4:  wr.println("<div class=\"md-h4\">" + escapeHtml(block.getText()) + "</div>"); break;
            default: wr.println("<div class=\"md-h4\">" + escapeHtml(block.getText()) + "</div>"); break;
        }
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
                final String styles = buildCssStyles(block.format(col));

                if (styles.isEmpty()) {
                    wr.println("<th class=\"" + alignClass + "\">" + header + "</th>");
                }
                else {
                    wr.println("<th style=\"" + styles + "\">" + header + "</th>");
                }
            }
            wr.println("</tr>");
            wr.println("</thead>");
        }

        // If we got two columns only and the first column has less than 25 chars
        // prevent wrapping words at '-' or ' ' char like ":throw-ex" or ":border-bottom s"
        final int[] colWitdhs = TextTableUtil.maxColWidths(block);
        final boolean noWrapFirstCol = (block.cols() == 2 && colWitdhs[0] < 25);

        wr.println("<tbody>");
        for(int row=0; row<block.bodyRows(); row++) {
            wr.println("<tr>");
            for(int col=0; col<block.cols(); col++) {
                final Chunks chunks = block.bodyCell(row, col);
                final String alignClass = buildCssAlignmentClass(block.format(col));
                final String styles = buildCssStyles(block.format(col));

                if (styles.isEmpty()) {
                    wr.print("<td class=\"" + alignClass + "\">");
                }
                else {
                    wr.print("<td style=\"" + styles + "\">");
                }

                if (col==0 && noWrapFirstCol) {
                    wr.print("<div style=\"display: inline-block; white-space: pre;\">");
                }

                render(chunks, wr);

                if (col==0 && noWrapFirstCol) {
                    wr.print("</div>");
                }

                wr.println("</td>");
            }
            wr.println("</tr>");
        }
        wr.println("</tbody>");

        wr.println("</table>");
        wr.println("</div>");
    }

    private void render(final Chunks chunks, final PrintWriter wr) {
        chunks.getChunks()
              .stream()
              .filter(c -> !c.isEmpty())
              .forEach(c -> { render(c, wr); });
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
        else if (c instanceof UrlChunk) {
            render((UrlChunk)c, wr);
        }
    }

    private void render(final TextChunk chunk, final PrintWriter wr) {
        final String text = escapeHtml(mapWhiteSpaces(chunk.getText()));
        final String formatClass = buildCssEmphasizeClass(chunk.getFormat());

        wr.print("<div class=\"" + formatClass + "\">" + text + "</div>");
    }

    private void render(final LineBreakChunk chunk, final PrintWriter wr) {
        wr.print("<br/>" );
    }

    private void render(final InlineCodeChunk chunk, final PrintWriter wr) {
        final String text = escapeHtml(chunk.getText());
        wr.print("<div class=\"md-inline-code\">" + text + "</div>");
    }

    private void render(final UrlChunk chunk, final PrintWriter wr) {
        final String url = chunk.getUrl();

        if (StringUtil.indexOneCharOf(url, "<>'\" ", 0) >= 0) {
            // invalid URL -> render as simple text
            render(new TextChunk(url), wr);
        }
        else if (chunk.getCaption().isEmpty()) {
            final String caption = escapeHtml(chunk.getUrl());
            wr.print("<a class=\"md-url\" href=\"" + url + "\">" + caption + "</a>");
        }
        else {
            final String caption = escapeHtml(chunk.getCaption());
            wr.print("<a class=\"md-url\" href=\"" + url + "\">" + caption + "</a>");
        }
    }

    private String buildCssEmphasizeClass(final TextChunk.Format format) {
        return "md-text-" + format.name().replace('_','-').toLowerCase();
    }

    private String buildCssAlignmentClass(final TableColFmt format) {
        final HorzAlignment alignment = format.horzAlignment();

        return "md-align-" + alignment.name().toLowerCase();
    }

    private String buildCssStyles(final TableColFmt format) {
        final String align = buildCssAlignmentStyle(format);
        final String width = buildCssWidthStyle(format);

        if (align.isEmpty()) {
            return width;
        }
        else if (width.isEmpty()) {
            return align;
        }
        else {
            return align + "; " + width;
        }
    }

    private String buildCssAlignmentStyle(final TableColFmt format) {
        final HorzAlignment alignment = format.horzAlignment();

        switch(alignment) {
            case LEFT:    return "text-align: left";
            case CENTER:  return "text-align: center";
            case RIGHT:   return "text-align: right";
            default:      return "";
        }
    }

    private String buildCssWidthStyle(final TableColFmt format) {
        final Width w = format.width();

        switch(w.getUnit()) {
            case AUTO:    return "";
            case PERCENT: return "width: " + w.getValue()+"%";
            case PX:      return "width: " + w.getValue()+"px";
            case EM:      return "width: " + w.getValue()+"em";
            default:      return "";
        }
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
