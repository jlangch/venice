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
package com.github.jlangch.venice.impl.util.markdown.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;


public class TextTableRendrer {

	public TextTableRendrer(
			final TableBlock block, 
			final int maxWidth
	) {
		this(block, maxWidth, ' ');
	}

	public TextTableRendrer(
			final TableBlock block, 
			final int maxWidth,
			final char fillChar
	) {
		this.block = block;
		this.maxWidth = maxWidth;
		this.fillChar = fillChar;
	}
	
	public String render() {
		final int cols = block.cols();
		
		final List<String> headerCells = toHeaderCellTextLines();
		final List<List<String>> bodyCells = toBodyCellTextLines();
		
		final int[] maxColWidths = maxColWidths(cols, headerCells, bodyCells);
		
		final int[] effColWidth = layoutColWidths(cols, maxColWidths);
		
		final List<String> lines = new ArrayList<>();
		
		if (block.hasHeader()) {
			lines.add(renderHeader(cols, effColWidth, headerCells));
			lines.add(renderHeaderSeparator(cols, effColWidth));
		}
		
		for(int row=0; row<bodyCells.size(); row++) {
			lines.addAll(renderBodyRow(cols, effColWidth, bodyCells.get(row)));
		}
		
		return lines
				.stream()
				.map(l -> StringUtil.trimRight(l))
				.collect(Collectors.joining("\n"));
	}
	
	private String renderHeader(
			final int cols,
			final int[] colWidth, 
			final List<String> headerCells
	) {
		final StringBuilder sb = new StringBuilder();
		for(int col=0; col<cols; col++) {
			if (col>0) sb.append(COL_SPACING);
			final String s = headerCells.get(col);
			sb.append(
				align(
					StringUtil.truncate(s, colWidth[col], "."),
					block.format(col),
					colWidth[col]));
		}
		return sb.toString();
	}

	private String renderHeaderSeparator(
			final int cols,
			final int[] colWidth
	) {
		final StringBuilder sb = new StringBuilder();
		for(int col=0; col<cols; col++) {
			if (col>0) sb.append(COL_SPACING);
			sb.append(StringUtil.repeat("-", colWidth[col]));
		}
		return sb.toString();
	}

	private List<String> renderBodyRow(
			final int cols,
			final int[] colWidth,
			final List<String> cells
	) {
		// wrap cell text
		final List<List<String>> cellLines = new ArrayList<>();
		for(int col=0; col<cols; col++) {
			cellLines.add(
				LineWrap.wrap(cells.get(col), colWidth[col]));
		}
		
		// fill up cell lines to an equals number of lines
		final int height = (int)cellLines.stream().mapToLong(l -> l.size()).max().orElse(0);
		for(int col=0; col<cols; col++) {
			for(int ii=cellLines.get(col).size(); ii<height; ii++) {
				cellLines.get(col).add("");
			}
		}		
		
		// align cell text
		for(int col=0; col<cols; col++) {
			final List<String> lines = cellLines.get(col);
			for(int ii=0; ii<lines.size(); ii++) {
				final String s = lines.get(ii);
				lines.set(ii, align(s, block.format(col), colWidth[col]));
			}
		}		
		
		// format
		final List<String> lines = new ArrayList<>();
		
		for(int ii=0; ii<height;ii++) {
			final StringBuilder sb = new StringBuilder();
			for(int col=0; col<cols; col++) {
				if (col>0) sb.append(COL_SPACING);
				final String chunk = cellLines.get(col).get(ii);
				sb.append(chunk);
			}
			lines.add(sb.toString());
		}
		
		return lines;
	}

	private int[] layoutColWidths(final int cols, final int[] maxColWidths) {
		final int usableWidth = maxWidth - (cols - 1) * COL_SPACING.length();

		if (sum(maxColWidths) <= usableWidth) {
			return maxColWidths;
		}
		else if (cols == 1) {
			return new int[] { maxWidth };
		}
		else {
			final int weight[] = new int[cols];
			
			// give every column a weight in the range 1..10
			int totalWeight = 0;
			for(int col=0; col<cols; col++) {
				final int w = Math.min(10, Math.max(1, maxColWidths[col] / 10));
				totalWeight += w;
				weight[col] = w;
			}
							

			final int widths[] = new int[cols];

			int restWidth = usableWidth;
			for(int col=0; col<cols; col++) {
				if (col < cols -1) {
					int width = Math.max(1, usableWidth * weight[col] / totalWeight);
					width = Math.min(maxColWidths[col], width);
					widths[col] = width;			
					restWidth -= width;
				}
				else {
					widths[col] = restWidth;			
				}
			}
			
			return widths;
		}
	}

	private int[] maxColWidths(
			final int cols, 
			final List<String> headerCells,
			final List<List<String>> bodyCells
	) {
		int[] widths = new int[block.cols()];

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

	private List<String> toHeaderCellTextLines() {
		final TextRenderer renderer = new TextRenderer(-1);
		
		final List<String> cols = new ArrayList<>();
		
		for(int col=0; col<block.cols(); col++) {
			cols.add(block.hasHeader() 
						? renderer.render(block.headerCell(col))
						: "");
		}
		
		return cols;
	}

	private List<List<String>> toBodyCellTextLines() {
		final TextRenderer renderer = new TextRenderer(-1);
		
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
	
	private String align(
			final String str, 
			final TableBlock.Alignment align,
			final int width
	) {
		switch(align) {
			case LEFT:	 return LineFormatter.leftAlign(str, width, fillChar);
			case CENTER: return LineFormatter.centerAlign(str, width, fillChar);
			case RIGHT:  return LineFormatter.rightAlign(str, width, fillChar);
			default:	 return LineFormatter.leftAlign(str, width, fillChar);
		}
	}
	
	private int sum(final int[] arr) {
		int sum = 0;
		for(int ii=0; ii<arr.length; ii++) sum += arr[ii];
		return sum;
	}
	
	
	private static final String COL_SPACING = "  ";

	private final TableBlock block;
	private final int maxWidth;
	private final char fillChar;
}
