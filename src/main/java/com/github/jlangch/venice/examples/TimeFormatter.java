/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.examples;

import java.util.List;
import java.util.stream.Collectors;

public class TimeFormatter {
	
	public static String formatNanos(final long nanos) {
		if (nanos < 1_000L) {
			return Long.valueOf(nanos).toString() + " ns";
		}
		else if (nanos < 1_000_000L) {
			return String.format("%.2f us", nanos / 1_000.0D);
		}
		else if (nanos < 9_000_000_000L) {
			return String.format("%.2f ms", nanos / 1_000_000.0D);
		}
		else {
			return String.format("%.2f s ", nanos / 1_000_000_000.0D);			
		}
	}
	
	public static long sum(final List<Long> measures) {
		return measures.stream().mapToLong(p -> p).sum();
	}
	
	public static List<Long> stripOutlier(final List<Long> measures) {
		return measures
					.stream()
					.sorted()
					.limit(measures.size() - measures.size() / 20) // top 20% are outliers
					.collect(Collectors.toList());
	}
}
