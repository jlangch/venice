/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.reader.LineReader;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.Markdown;
import com.github.jlangch.venice.impl.util.markdown.block.Block;
import com.github.jlangch.venice.impl.util.markdown.block.CodeBlock;
import com.github.jlangch.venice.impl.util.markdown.block.ListBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TextBlock;
import com.github.jlangch.venice.impl.util.markdown.block.TitleBlock;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.Chunks;
import com.github.jlangch.venice.impl.util.markdown.chunk.InlineCodeChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.LineBreakChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.TextChunk;
import com.github.jlangch.venice.impl.util.markdown.chunk.UrlChunk;


public class TextRenderer {

    private TextRenderer(
            final int width,
            final boolean softWrap
    ) {
        this.width = width;
        this.softWrap = softWrap;
    }

    /**
     * Creates a renderer without line wraps.
     */
    public TextRenderer() {
    }

    /**
     * Configures the renderer for hard wraps lines at 'width' characters.
     *
     * @param width The max line width. Must be greater than 0.
     * @return This renderer
     */
    public TextRenderer hardWrap(final int width) {
        if (width < 1) {
            throw new IllegalArgumentException("A wrap width must be positive");
        }

        this.width = width;
        this.softWrap = false;

        return this;
    }

    /**
     * Configures the renderer for soft wraps lines at 'width' characters.
     * Tries to wrap at the closest whitespace character and if not possible
     * falls back to a hard wrap.
     *
     * @param width The max line width. Must be greater than 0.
     * @return This renderer
     */
    public TextRenderer softWrap(final int width) {
        if (width < 1) {
            throw new IllegalArgumentException("A wrap width must be positive");
        }


        this.width = width;
        this.softWrap = true;

        return this;
    }

    /**
     * Configures the renderer without line wraps.
     *
     * @return This renderer
     */
    public TextRenderer nowrap() {
        this.width = -1;
        this.softWrap = false;

        return this;
    }

    /**
     * Renders the markdown
     *
     * @param md the markdown
     * @return the rendered markdown
     */
    public String render(final Markdown md) {
        final StringBuilder sb = new StringBuilder();

        for(Block b : md.blocks().getBlocks()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }

            sb.append(render(b));
        }

        return sb.toString().replace("\u00A0", " ");
    }

    public String render(final Block b) {
        if (b.isEmpty()) {
            return "";
        }
        else if (b instanceof TitleBlock) {
            return render((TitleBlock)b);
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

            if (c instanceof TextChunk) {
                sb.append(render((TextChunk)c));
            }
            else if (c instanceof LineBreakChunk) {
                sb.append(render((LineBreakChunk)c));
            }
            else if (c instanceof InlineCodeChunk) {
                sb.append(render((InlineCodeChunk)c));
            }
            else if (c instanceof UrlChunk) {
                sb.append(render((UrlChunk)c));
            }
        }

        String s = sb.toString();
        s = isWrap() ? wrap(s, width) : s;
        s = s.replace("\u00A0", " ");
        return s;
    }

    private String render(final TitleBlock block) {
        return "\n" + block.getText() + "\n";
    }

    private String render(final TextBlock block) {
        return render(block.getChunks());
    }

    private String render(final CodeBlock block) {
        return block.getLines()
                    .stream()
                    .map(l -> CODE_INDENT + l)
                    .collect(Collectors.joining("\n"));
    }

    private String render(final ListBlock block) {
        final StringBuilder sb = new StringBuilder();

        final int itemNrDigits = digits(block.size());

        for(int ii=0; ii<block.size(); ii++) {
            Block b = block.get(ii);

            if (sb.length() > 0) {
                sb.append("\n");
            }

            final String prefix =
                    block.isOrdered()
                        ? LIST_INDENT + formatListItemNr(ii+1, itemNrDigits) + " "
                        : LIST_INDENT + BULLET + " ";

            final int blockWidth = width-prefix.length();

            if (isWrap()) {
                sb.append(
                    indent(
                        new TextRenderer(blockWidth, softWrap).render(b),
                        prefix,
                        StringUtil.repeat(' ', prefix.length())));
            }
            else {
                sb.append(prefix + render(b));
            }
        }

        return sb.toString();
    }

    private String render(final TableBlock block) {
        return new TextTableRendrer(block, width).render();
    }

    private String render(final TextChunk chunk) {
        String s = chunk.getText();
        s = StringUtil.replace(s, "\t", "\u00A0\u00A0\u00A0\u00A0", -1, false);
        s = StringUtil.replace(s, "&nbsp;", "\u00A0", -1, false);
        s = StringUtil.replace(s, "&ensp;", "\u00A0\u00A0", -1, false);
        s = StringUtil.replace(s, "&emsp;", "\u00A0\u00A0\u00A0\u00A0", -1, false);
        return s;
    }

    private String render(final LineBreakChunk chunk) {
        return "\n";
    }

    private String render(final InlineCodeChunk chunk) {
        return chunk.getText();
    }

    private String render(final UrlChunk chunk) {
        if (chunk.isEmpty()) {
            return "";
        }
        else {
            return chunk.getCaption().isEmpty()
                ? chunk.getUrl()
                : chunk.getCaption() + " (" + chunk.getUrl() + ")";
        }
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

    private String formatListItemNr(final int itemNr, final int digits) {
        final String s = String.format("%d", itemNr);
        return s + '.' + StringUtil.repeat(' ', Math.max(0, digits-s.length()));
    }

    private int digits(final int x) {
        return (int)Math.log10(x);
    }


    private static final char BULLET = 'o';

    private static final String CODE_INDENT = "\u00A0\u00A0\u00A0";
    private static final String LIST_INDENT = "\u00A0\u00A0";

    private int width = -1;
    private boolean softWrap = true;
}
