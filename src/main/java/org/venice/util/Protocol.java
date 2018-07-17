package org.venice.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.venice.impl.util.StringUtil;


public class Protocol {

	private enum Level { DEBUG, INFO, WARN, ALERT };
	
	
	public Protocol() {
		this(true, true);
	}

	public Protocol(final boolean enabled) {
		this(enabled, false);
	}

	public Protocol(
			final boolean enabled, 
			final boolean decorateWithTimestamp
	) {
		this.enabled = enabled;
		this.decorateWithTimestamp = decorateWithTimestamp;
	}
	
	public void debugOn() {
		debugOn.set(true);
	}

	public void debugOff() {
		debugOn.set(false);
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
			
			synchronized(sb) {
				// A very simple protection against malicious scripts
				if (sb.length() + m.length() < MAX_PROTOCOL_SIZE) {
					sb.append(m);
				}
			}
		}
	}

	public void setStatistics(final String name, final int value) {
		if (!StringUtil.isBlank(name) && value >= 0) {
			synchronized(statistics) {
				statistics.put(name, value);
			}
		}
	}

	public void incStatistics(final String name) {
		incStatistics(name, 1);
	}
	
	public void incStatistics(final String name, final int value) {
		if (!StringUtil.isBlank(name) && value > 0) {
			synchronized(statistics) {
				final Integer val = statistics.get(name);
				statistics.put(name, val==null ? value : val.intValue() + value);
			}
		}
	}

	public int getStatistics(final String name) {
		if (!StringUtil.isBlank(name)) {
			synchronized(statistics) {
				final Integer val = statistics.get(name);
				return val == null ? 0 : val.intValue();
			}
		}
		else {
			return 0;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isEmpty() {
		synchronized(sb) {
			return sb.length() == 0;
		}
	}

	public String mergeWithText(final Object text, final boolean addStatistics) {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(text == null ? "No result" : text.toString());
		
		if (isEnabled()) {
			synchronized(statistics) {
				if (addStatistics && !statistics.isEmpty()) {
					sb.append("\n\n\n")
					  .append(formatStatistics(statistics));
				}
			}
			if (!isEmpty()) {
				sb.append("\n\n\n")
				  .append("Protocol:\n")
				  .append(this);
			} 
		}
		
		return sb.toString();
	}
	
	public String mergeWithText(final Object text) {
		return mergeWithText(text, false);
	}

	@Override
	public String toString() {
		synchronized(sb) {
			return sb.toString();
		}
	}
	
	private List<String> getSortedStatisticsKeys() {
		final List<String> keys = new ArrayList<>(getStatisticsKeys());
		Collections.sort(keys);
		return keys;
	}

	private Set<String> getStatisticsKeys() {
		synchronized(statistics) {
			return statistics.keySet();
		}
	}

	
	private String formatStatistics(final Map<String,Integer> statistics) {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Statistics:\n");
		for(String key : getSortedStatisticsKeys()) {
			sb.append(key).append(": ")
			  .append(statistics.get(key)).append("\n");
		}
		
		return sb.toString();
	}
	
	private String filter(final String text) {
		return text.replace("\r", "")
				   .replace("\n", "\n" + leftPad("", decorateWithTimestamp ? 31 : 7));
	}
	
	private String getPrefix(final Level level) {
		final StringBuilder sb = new StringBuilder();		
		sb.append("[");
		
		if (decorateWithTimestamp) {
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

	
	private final int MAX_PROTOCOL_SIZE = 20 * 1024 * 1024; // 20MB
	
	private final boolean enabled;
	private final boolean decorateWithTimestamp;

	// thread safety: the sb object is used as monitor
	private final StringBuilder sb = new StringBuilder();
	
	// thread safety: the statistics object is used as monitor
	private final Map<String,Integer> statistics = new HashMap<>();
	
	private final AtomicBoolean debugOn = new AtomicBoolean(false);

	private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
}
