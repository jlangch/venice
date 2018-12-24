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
package com.github.jlangch.venice.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class Logger {

	private enum Level { DEBUG, INFO, WARN, ALERT };
	
	
	public Logger() {
	}
	
	public void attachOutputStream(final OutputStream os) {
		this.ps.set(os == null ? null : createPrintStream(os));
	}

	public void decorateWithTimestamp(final boolean enable) {
		this.decorateWithTimestamp.set(enable);
	}

	public void enable(final boolean enable) {
		this.enabled.set(enable);
	}

	public void debugOn() {
		debugOn.set(true);
	}

	public void debugOff() {
		debugOn.set(false);
	}

	public void clear() {
		synchronized(sb) {
			sb.setLength(0);
		}
	}

	public void log(final Level level, final String text) {
		log(level, text, null);
	}
	
	public void log(final Level level, final String text, final Exception ex) {
		if (isEnabled() 
				&& (text != null || ex != null) 
				&& (safeLevel(level) != Level.DEBUG || debugOn.get())
		) {
			final StringBuilder m = new StringBuilder();
			m.append(getPrefix(safeLevel(level)));
			if (text != null) {
				m.append(filter(text)).append('\n');
			}
			if (ex != null) {
				m.append(getExceptionStackTrace(ex)).append('\n');
			}
			
			logMsg(m.toString());
		}
	}

	public boolean isEnabled() {
		return enabled.get();
	}
	
	public boolean isEmpty() {
		synchronized(sb) {
			return sb.length() == 0;
		}
	}

	@Override
	public String toString() {
		synchronized(sb) {
			return sb.toString();
		}
	}
	
	private void logMsg(final String msg) {
		synchronized(msg) {
			// A very simple protection against malicious scripts
			if (sb.length() + msg.length() < MAX_PROTOCOL_SIZE) {
				sb.append(msg);
			}
			
			if (ps.get() != null) {
				ps.get().print(msg);
			}
		}
	}
	
	private String filter(final String text) {
		return text.replace("\r", "")
				   .replace("\n", "\n" + leftPad("", decorateWithTimestamp.get() ? 31 : 7));
	}
	
	private String getPrefix(final Level level) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		
		if (decorateWithTimestamp.get()) {
			// timestamp
			sb.append(LocalDateTime.now().format(dtFormatter)).append("|");
		}
		
		// level
		sb.append(getLevelString(level));
		
		sb.append("] ");
		return sb.toString();
	}
	
	private String getExceptionStackTrace(final Exception ex) {
		if (ex instanceof SecurityException) {
			// do not reveal details of a SecurityException
			return getSafeExceptionStackTrace(ex);
		}
		else {
			// full stack trace
			return getStackTrace(ex);
		}
	}
	
	private String getSafeExceptionStackTrace(final Exception ex) {
		final StringBuilder msg = new StringBuilder();
		
		msg.append(getSafeExceptionMessage(ex));
		
		Throwable th = ex.getCause();
		while(th != null) {
			msg.append("\nCaused by: ")
			   .append(getSafeExceptionMessage(ex));
			th = th.getCause();
		}
		
		return msg.toString();
	}

	private String getSafeExceptionMessage(final Throwable ex) {
		final StringBuilder msg = new StringBuilder();

		msg.append(ex.getClass());
		if (ex.getMessage() != null) {
			msg.append(": ").append(ex.getMessage());
		}

		return msg.toString();
	}

	private String getLevelString(final Level level) {
		switch(level) {
			case DEBUG: return "DEBG";
			case INFO:  return "INFO";
			case WARN:  return "WARN";
			case ALERT: return "ALRT";
			default:    return "INFO";
		}
	}
	
	private String leftPad(final String text, final int width) {
		if (text.length() >= width) {
			return text;
		}
		else {
			final StringBuilder sb = new StringBuilder();
			for(int ii=text.length(); ii< width; ii++) {
				sb.append(' ');
			}
			sb.append(text);
			return sb.toString();
		}
	}

	private String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
	
	private Level safeLevel(final Level level) {
		return level == null ? Level.DEBUG : level;
	}

	private PrintStream createPrintStream(final OutputStream os) {
		try {
			return (os instanceof PrintStream)
						? (PrintStream)os
						: new PrintStream(os, true, "UTF-8");
		}
		catch(UnsupportedEncodingException ex) {
			throw new RuntimeException("Unsupported encoding UTF-8", ex);
		}
	}

	
	private final int MAX_PROTOCOL_SIZE = 20 * 1024 * 1024; // 20MB
	
	// thread safety: the sb object is used as monitor
	private final StringBuilder sb = new StringBuilder();
	
	private final AtomicReference<PrintStream> ps = new AtomicReference<>();
	
	private final AtomicBoolean debugOn = new AtomicBoolean(false);
	private final AtomicBoolean enabled = new AtomicBoolean(true);
	private final AtomicBoolean decorateWithTimestamp = new AtomicBoolean(true);

	private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
}
