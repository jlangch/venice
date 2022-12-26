/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;


public class TextTableUtil {

    public static int[] maxColWidths(final TableBlock block) {
        return maxColWidths(
                block,
                toHeaderCellTextLines(block),
                toBodyCellTextLines(block));
    }

    public static int[] maxColWidths(
            final TableBlock block,
            final List<String> headerCells,
            final List<List<String>> bodyCells
    ) {
        final int cols = block.cols();

        int[] widths = new int[cols];

        for(int ii=0; ii<block.cols(); ii++) {
            widths[ii] = 0;
        }

        // header
        for(int ii=0; ii<cols; ii++) {
            widths[ii] = Math.max(widths[ii], headerCells.get(ii).length());
        }

        // body
        bodyCells.forEach(row -> {
            for(int ii=0; ii<cols; ii++) {
                widths[ii] = Math.max(widths[ii], row.get(ii).length());
            }
        });

        return widths;
    }

    public static List<String> toHeaderCellTextLines(final TableBlock block) {
        final TextRenderer renderer = new TextRenderer().nowrap();

        final List<String> cols = new ArrayList<>();

        for(int col=0; col<block.cols(); col++) {
            cols.add(block.hasHeader()
                        ? renderer.render(block.headerCell(col))
                        : "");
        }

        return cols;
    }

    public static List<List<String>> toBodyCellTextLines(final TableBlock block) {
        final TextRenderer renderer = new TextRenderer().nowrap();

        final List<List<String>> cells = new ArrayList<>();

        for(int row=0; row<block.bodyRows(); row++) {
            final List<String> cols = new ArrayList<>();
            for(int col=0; col<block.cols(); col++) {
                cols.add(renderer.render(block.bodyCell(row, col)));
            }
            cells.add(cols);
        }

        return cells;
    }

}
