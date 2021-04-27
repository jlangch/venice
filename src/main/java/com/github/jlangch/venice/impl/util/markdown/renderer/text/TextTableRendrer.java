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
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.markdown.block.TableBlock;


public class TextTableRendrer {

	public TextTableRendrer(
			final TableBlock block, 
			final int maxTableWidth
	) {
		this(block, maxTableWidth, ' ', "  ");
	}

	public TextTableRendrer(
			final TableBlock block, 
			final int maxTableWidth,
			final char fillChar,
			final String colSpacing
	) {
		this.block = block;
		this.maxTableWidth = maxTableWidth;
		this.fillChar = fillChar;
		this.colSpacing = colSpacing;
	}
	
	public String render() {
		final int cols = block.cols();
		
		final List<String> headerCells = toHeaderCellTextLines();
		final List<List<String>> bodyCells = toBodyCellTextLines();
		
		final int[] maxColWidths = maxColWidths(cols, headerCells, bodyCells);
		
		final int[] effColWidth = new TextTableLayouter().layoutColWidths(
															maxTableWidth, 
															colSpacing.length(),
															maxColWidths);
		
		final List<String> lines = new ArrayList<>();
		
		if (block.hasHeader()) {
			lines.add(renderHeader(cols, effColWidth, headerCells));
			lines.add(renderHeaderSeparator(cols, effColWidth));
		}
		
		for(int row=0; row<bodyCells.size(); row++) {
			lines.addAll(renderBodyRow(cols, effColWidth, bodyCells.get(row)));
		}
		
		return lines.stream()
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
			if (col>0) sb.append(colSpacing);
			int width = colWidth[col];
			final String s = headerCells.get(col);
			sb.append(
				align(
					s.length() <= width ? s : s.substring(0, width),
					block.format(col),
					width));
		}
		return sb.toString();
	}

	private String renderHeaderSeparator(
			final int cols,
			final int[] colWidth
	) {
		final StringBuilder sb = new StringBuilder();
		for(int col=0; col<cols; col++) {
			if (col>0) sb.append(colSpacing);
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
				if (col>0) sb.append(colSpacing);
				final String chunk = cellLines.get(col).get(ii);
				sb.append(chunk);
			}
			lines.add(sb.toString());
		}
		
		return lines;
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
	
	
	private final TableBlock block;
	private final int maxTableWidth;
	private final String colSpacing;
	private final char fillChar;
}
