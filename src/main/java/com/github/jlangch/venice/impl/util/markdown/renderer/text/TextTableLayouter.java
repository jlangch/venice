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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.jlangch.venice.impl.util.Tuple2;

public class TextTableLayouter {

	public TextTableLayouter() {
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

			return new int[] { Math.min(maxColWidths[0], maxTableWidth) };
		}
		else {
			// more than one column
			
			final double weight[] = new double[cols];
			
			// give every column a weight for its width in the range 1..10
			for(int col=0; col<cols; col++) {
				final double weightedWidth = (double)maxColWidths[col] / (double)usableWidth * 10D;
				weight[col] = clip(weightedWidth, 1D, 10D);
			}

			final double totalWeight = Arrays.stream(weight).sum();

			final int widths[] = new int[cols];
			Arrays.fill(widths, -1);

			// order columns by primary width ascending and secondary column nr ascending
			List<Tuple2<Integer,Integer>> colsOrdered = 
					IntStream.range(0, maxColWidths.length)
							 .mapToObj(idx -> new Tuple2<Integer,Integer>(idx, maxColWidths[idx]))
							 .sorted(Comparator
										.comparing((Tuple2<Integer,Integer> t) -> t._2)
										.thenComparing((Tuple2<Integer,Integer> t) -> t._1))
							 .collect(Collectors.toList());

			int restWidth = usableWidth;
			while(!colsOrdered.isEmpty()) {
				int col = colsOrdered.get(0)._1;

				if (colsOrdered.size() == 1) {
					widths[col] = restWidth; // last column fill up
				}
				else {
					int width = (int)((double)restWidth * weight[col] / totalWeight);
					width = clip(width, 1, maxColWidths[col]);
					widths[col] = width;
					restWidth -= width;
				}
				colsOrdered = colsOrdered.subList(1, colsOrdered.size());
			}

			return widths;
		}
	}

	private int clip(final int value, final int min, final int max) {
		return Math.min(max, Math.max(min, value));
	}

	private double clip(final double value, final double min, final double max) {
		return Math.min(max, Math.max(min, value));
	}
}
