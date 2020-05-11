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
package com.github.jlangch.venice.impl.repl;

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.nanojson.JsonArray;
import com.github.jlangch.venice.nanojson.JsonObject;
import com.github.jlangch.venice.nanojson.JsonParser;


/**
 * REPL configuration
 * 
 * ANSI terminal colors: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html#colors	
 */
public class ReplConfig {

	private ReplConfig(final Map<String,Object> config) {
		this.config = config;
	}
	
	public static ReplConfig load(final CommandLineArgs cli) {
		final Map<String,Object> config = new HashMap<>();
		
		// use colors
		config.put("colors.mode", getColorMode(cli));
		
		// load module
		final String file = cli.switchValue("-load-file");
		if (file != null) {
			config.put("load.file", file);
		}

		try {
			final JsonObject jsonObj = loadJsonConfig();
			config.put("prompt", jsonObj.getString("prompt"));
			config.put("secondary-prompt", jsonObj.getString("secondary-prompt"));
			config.put("result-prefix", jsonObj.getString("result-prefix"));

			JsonObject obj = (JsonObject)jsonObj.get("colors");
			if (obj != null) {
				for(String cname : COLOR_NAMES) {
					config.put("colors." + cname, StringUtil.emptyToNull(obj.getString(cname)));
				}
			}

			obj = (JsonObject)jsonObj.get("colors-darkmode");
			if (obj != null) {
				for(String cname : COLOR_NAMES) {
					config.put("colors-darkmode." + cname, StringUtil.emptyToNull(obj.getString(cname)));
				}
			}
			
			obj = (JsonObject)jsonObj.get("jline");
			if (obj != null) {
				config.put("jline.loglevel", obj.getString("loglevel"));
				config.put("jline.dumb-terminal", obj.getBoolean("dumb-terminal", Boolean.FALSE));
			}

			obj = (JsonObject)jsonObj.get("libs");
			if (obj != null) {
				config.put("libs.dir", obj.getString("dir"));
				config.put("libs.mandatory", getStringList(obj.getArray("mandatory")));
				config.put("libs.optional", getStringList(obj.getArray("optional")));
			}

			obj = (JsonObject)jsonObj.get("fonts");
			if (obj != null) {
				config.put("fonts.dir", obj.getString("dir"));
				config.put("fonts.base-url", obj.getString("base-url"));
				config.put("fonts.mandatory", getStringList(obj.getArray("mandatory")));
				config.put("fonts.optional", getStringList(obj.getArray("optional")));
			}

			return new ReplConfig(config);
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to parse REPL json config file", ex);
		}	
	}
	
	public String getColor(final String key) {
		switch(getString("colors.mode")) {
			case "light": return getString("colors." + key);
			case "dark":  return getString("colors-darkmode." + key);
			default:      return null;
		}
	}

	public String getLoadFile() {
		return getString("load.file");
	}

	public String getPrompt() {
		final String prompt = getStringOrDefault("prompt", DEFAULT_PROMPT);
		return getColor("prompt") == null
				? prompt
				: getColor("prompt") + prompt + ReplConfig.ANSI_RESET;
	}

	public String getSecondaryPrompt() {
		final String prompt = getStringOrDefault("secondary-prompt", DEFAULT_SECONDARY_PROMPT);
		return getColor("secondary-prompt") == null
				? prompt
				: getColor("secondary-prompt") + prompt + ReplConfig.ANSI_RESET;
	}

	public String getResultPrefix() {
		return getStringOrDefault("result-prefix", DEFAULT_RESULT_PREFIX);
	}
	
	public Level getJLineLogLevel() {
		try {
			return Level.parse(getString("jline.loglevel"));
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	public boolean isJLineDumbTerminal() {
		try {
			return (Boolean)config.get("jline.dumb-terminal");
		}
		catch(Exception ex) {
			return false;
		}
	}
	
	public String getJansiVersion() {
		try (InputStream is = getClass()
								.getClassLoader()
								.getResourceAsStream("org/fusesource/jansi/Ansi/jansi.properties")
		) {
			if (is != null) {
				final Properties props = new Properties();
				props.load(is);
				return props.getProperty("version");
			}
		} 
		catch (IOException e) {
			// Ignore
		}
		return null;
	}
	
	public String getLibsDir() {
		return getString("libs.dir");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLibsMandatory() {
		return nullToEmptyList((List<String>)config.get("libs.mandatory"));
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLibsOptional() {
		return nullToEmptyList((List<String>)config.get("libs.optional"));
	}
	
	public String getFontsDir() {
		return getString("fonts.dir");
	}
	
	public String getFontsBaseUrl() {
		return getString("fonts.base-url");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getFontsMandatory() {
		return nullToEmptyList((List<String>)config.get("fonts.mandatory"));
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getFontsOptional() {
		return nullToEmptyList((List<String>)config.get("fonts.optional"));
	}

	public static String getRawClasspathConfig() {
		return new ClassPathResource(getVeniceBasePath() + "repl.json")
						.getResourceAsString("UTF-8");
	}

	public static String getRawClasspathLauncherName() {
		return System.getProperty("os.name").startsWith("Windows") ? "repl.bat" : "repl.sh";
	}

	public static String getRawClasspathLauncher() {
		return new ClassPathResource(getVeniceBasePath() + getRawClasspathLauncherName())
						.getResourceAsString("UTF-8");
	}


	private String getString(final String key) {
		return StringUtil.emptyToNull((String)config.get(key));
	}
	
	private String getStringOrDefault(final String key, final String defaultValue) {
		final String val = getString(key);
		return val == null ? defaultValue : val;
	}
	
	private static List<String> getStringList(final JsonArray array) {
		final List<String> values = new ArrayList<>();
		array.forEach(v -> values.add((String)v));
		return values;
	}

	private static JsonObject loadJsonConfig() throws Exception {
		final File fileJson = new File("repl.json");

		if (fileJson.isFile()) {
			System.out.println("Loading REPL config from " + fileJson + "...");
			try (Reader reader = new FileReader(fileJson)) {
				return (JsonObject)JsonParser.object().from(reader);
			}
		}
		else {
			final String config = getRawClasspathConfig();
			System.out.println("Loading REPL default config...");
			return (JsonObject)JsonParser.object().from(config);
		}
	}
	
	private static String getColorMode(final CommandLineArgs cli) {
		if (cli.switchPresent("-colors")) {
			return "light";
		}
		else if (cli.switchPresent("-colors-lightmode")) {
			return "light";
		}
		else if (cli.switchPresent("-colors-darkmode")) {
			return "dark";
		}
		else {
			return "none";
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> List<T> nullToEmptyList(final List<T> items) {
		return items == null ? (List<T>) Collections.emptyList() : items;
	}

	
	
	public static final String ANSI_RESET = "\u001b[0m";

	private static final String DEFAULT_PROMPT = "venice> ";
	private static final String DEFAULT_SECONDARY_PROMPT = "| ";
	private static final String DEFAULT_RESULT_PREFIX = "=> ";
	
	private static final List<String> COLOR_NAMES = Arrays.asList(
														"result", 
														"stdout", 
														"stderr", 
														"error", 
														"system", 
														"interrupt", 
														"prompt");

	private final Map<String,Object> config;
}
