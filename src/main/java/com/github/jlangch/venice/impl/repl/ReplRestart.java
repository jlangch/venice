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
package com.github.jlangch.venice.impl.repl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;


public class ReplRestart {
	
	private ReplRestart(final List<String> lines) {
		this.lines = lines;
		this.createdAt = lines.isEmpty() ? null : parse(lines.get(0));
	}
	
	public static ReplRestart read() {
		try {
			return new ReplRestart(Files.readAllLines(RESTART_FILE.toPath()));
		}
		catch(Exception ex) {
			return new ReplRestart(new ArrayList<>());
		}
	}
	
	public static void write(final boolean macroEpxandOnLoad) {
		try {
			final List<String> lines = new ArrayList<>();
			
			if (macroEpxandOnLoad) {
				lines.add(format(LocalDateTime.now()));
				lines.add("-macropexand");
			}
			
			Files.write(
					RESTART_FILE.toPath(), 
					lines, 
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch(Exception ex) {
			// skipped (best effort)
		}
	}

	public static boolean exists() {
		try {
			return RESTART_FILE.exists();
		}
		catch(Exception ex) {
			return false;
		}
	}

	public static void remove() {
		try {
			RESTART_FILE.delete();
		}
		catch(Exception ex) {
			// skipped (best effort)
		}
	}

	public boolean hasMacroExpand() {
		try {
			return lines.stream().anyMatch(s -> s.trim().equals("-macropexand"));
		}
		catch(Exception ex) {
			return false;
		}
	}

	public boolean oudated() {
		return createdAt == null || ChronoUnit.DAYS.between(createdAt, readAt) > 30L;
	}
	
	private static String format(final LocalDateTime dt) {
		return dtFormatter.format(dt);
	}
	
	private static LocalDateTime parse(final String s) {
		try {
			return LocalDateTime.parse(s, dtFormatter);
		}
		catch (Exception ex) {
			return null;
		}
	}

	
	private final static File RESTART_FILE = new File(".repl.restart");
	private final static DateTimeFormatter dtFormatter = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); 
	
	private final List<String> lines;
	private final LocalDateTime createdAt;
	private final LocalDateTime readAt = LocalDateTime.now();
}
