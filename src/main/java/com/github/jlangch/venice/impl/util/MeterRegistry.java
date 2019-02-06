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
package com.github.jlangch.venice.impl.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.util.Timer;


public class MeterRegistry implements Serializable {

	public MeterRegistry(final boolean enabled) {
		this.enabled = enabled;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public void disable() {
		this.enabled = false;
	}
	
	public void reset() {
		this.data.clear();
	}
	
	public void record(final String name, final long elapsedTime) {
		data.compute(name, (k, v) -> v == null 
										? new Timer(name, 1, elapsedTime) 
										: v.add(elapsedTime));
	}
	
	public Collection<Timer> getTimerData() {
		return data.values();
	}

	public String getTimerDataFormatted(final String title) {
		final Collection<Timer> data = getTimerData();
		
		final int maxNameLen = data
								.stream()
								.mapToInt(v -> v.name.length())
								.max()
								.orElse(10);

		final int maxCount = data
								.stream()
								.mapToInt(v -> v.count)
								.max()
								.orElse(10);

		final int maxCountLen = Integer.valueOf(maxCount).toString().length();

		final List<String> lines =
				data.stream()
					.sorted((u,v) -> Long.valueOf(v.elapsedNanos).compareTo(u.elapsedNanos))
					.map(v -> String.format("%-" + maxNameLen +"s  [%" + maxCountLen + "d]: %10s", v.name, v.count, Timer.formatNanos(v.elapsedNanos)))
					.collect(Collectors.toList());

		if (lines.isEmpty()) {
			lines.add("no meter data!");
		}
		
		if (!StringUtil.isBlank(title)) {
			final int maxLineLen = Math.max(
									title.length(), 
									lines
										.stream()
										.mapToInt(l -> l.length())
										.max()
										.orElse(0));
			
			final String delim = String.join("", Collections.nCopies(maxLineLen, "-"));
			lines.add(0, delim.toString());
			lines.add(0, title);
			lines.add(0, delim.toString());
			lines.add(delim.toString());
		}

		return String.join("\n", lines);
	}
	

	private static final long serialVersionUID = 5426843508785133806L;

	
	private final Map<String,Timer> data = new ConcurrentHashMap<>();
	
	public volatile boolean enabled;
}
