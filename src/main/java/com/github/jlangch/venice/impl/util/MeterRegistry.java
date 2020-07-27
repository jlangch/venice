/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.util.Timer;


public class MeterRegistry implements Serializable {

	public MeterRegistry(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public void reset() {
		data.clear();
	}
	
	public void resetAllBut(final VncSequence records) {
		final Map<String,Timer> keep = 
				records.getList()
					   .stream()
					   .map(r -> data.get(Coerce.toVncString(r).getValue()))
					   .filter(t -> t != null)
					   .collect(Collectors.toMap(Timer::getName, Function.identity()));

		data.clear();
		data.putAll(keep);
	}
	
	public void record(final String name, final long elapsedTime) {
		if (elapsedTime > 0) {
			data.compute(
					name, 
					(k, v) -> v == null 
								? new Timer(name, elapsedTime) 
								: v.add(elapsedTime));
		}
	}
	
	public Collection<Timer> getTimerData() {
		return data.values();
	}
	
	public VncList getVncTimerData() {
		return VncList.ofList(
					getTimerData()
						.stream()
						.map(t -> convertToVncMap(t))
						.collect(Collectors.toList()));
	}

	public String getTimerDataFormatted(
			final String title, 
			final boolean withAnonymousFunctions
	) {
		final Collection<Timer> data = withAnonymousFunctions 
										? getTimerData()
										: getTimerData()
											.stream()
											.filter(t -> !t.name.contains("anonymous-"))
											.collect(Collectors.toList());
		
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
					.map(v -> format(v, maxNameLen, maxCountLen))
					.collect(Collectors.toList());

		if (lines.isEmpty()) {
			lines.add("no meter data!");
		}
		
		if (!StringUtil.isBlank(title)) {
			final int maxLineLen = Math.max(
									title.length(), 
									lines.stream()
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
	
	private String format(final Timer t, final int maxNameLen, final int maxCountLen) {
		return String.format(
					"%-" + maxNameLen +"s  [%" + maxCountLen + "d]: %11s %11s", 
					t.name, 
					t.count, 
					Timer.formatNanos(t.elapsedNanos),
					t.count == 1 ? "" : Timer.formatNanos(t.elapsedNanos / t.count));	
	}
	
	private VncMap convertToVncMap(final Timer timer) {
		return VncHashMap.of(
				new VncKeyword("name"),  new VncString(timer.name),
				new VncKeyword("count"), new VncLong(timer.count),
				new VncKeyword("nanos"), new VncLong(timer.elapsedNanos));
	}

	
	private static final long serialVersionUID = 5426843508785133806L;
	
	private final Map<String,Timer> data = new ConcurrentHashMap<>();
	
	public volatile boolean enabled;
}
