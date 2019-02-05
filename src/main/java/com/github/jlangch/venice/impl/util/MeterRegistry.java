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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	
	private static final long serialVersionUID = 5426843508785133806L;

	
	private final Map<String,Timer> data = new ConcurrentHashMap<>();
	
	public volatile boolean enabled;
}
