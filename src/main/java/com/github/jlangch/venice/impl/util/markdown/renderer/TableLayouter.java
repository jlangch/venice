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

import java.util.Arrays;

public class TableLayouter {

	public TableLayouter() {
	}


	public int[] layoutColWidths(
			final int maxTableWidth,
			final int colSpacing,
			final int[] maxColWidths
	) {
		final int cols = maxColWidths.length;
		final int usableWidth = maxTableWidth - (cols - 1) * colSpacing;

		if (Arrays.stream(maxColWidths).sum() <= usableWidth) {
			// all columns fit within 'maxTableWidth'
			return maxColWidths;
		}
		else if (cols == 1) {
			// single column -> use up all space available
			return new int[] { maxTableWidth };
		}
		else {
			final double weight[] = new double[cols];
			
			// give every column a weight in the range 1..10
			for(int col=0; col<cols; col++) {
				weight[col] = Math.min(10, Math.max(1, maxColWidths[col] / 10));
			}
			
			final double totalWeight = Arrays.stream(weight).sum();

			final int widths[] = new int[cols];
			Arrays.fill(widths, -1);

			int restWidth = usableWidth;
			while(true) {
				int col = findMostNarrowColumnNotAssigned(widths, maxColWidths);
				if (col < 0) break;
				
				int unassignedCols = countColumnsNotAssigned(widths);
				if (unassignedCols == 1) {
					widths[col] = restWidth; // last column fill up
					break;
				}
				else {
					int width = Math.max(1, (int)((double)restWidth * weight[col] / totalWeight));
					width = Math.min(maxColWidths[col], width);
					widths[col] = width;
					restWidth -= width;
				}
			}
			
			return widths;
		}
	}
	
	private int countColumnsNotAssigned(final int assignedWidths[]) {
		int count = 0;
		for(int col=0; col<assignedWidths.length; col++) {
			if (assignedWidths[col] < 0) count++;
		}
		return count;
	}

	private int findMostNarrowColumnNotAssigned(
			final int assignedWidths[],
			final int[] maxColWidths
	) {
		int mostNarrowColWidth = Integer.MAX_VALUE;
		int mostNarrowCol = -1;
		
		for(int col=0; col<maxColWidths.length; col++) {
			int colWidth = maxColWidths[col];
			if (assignedWidths[col] < 0 && colWidth < mostNarrowColWidth) {
				mostNarrowColWidth = colWidth;
				mostNarrowCol = col;
			}
		}
		
		return mostNarrowCol;
	}
}
