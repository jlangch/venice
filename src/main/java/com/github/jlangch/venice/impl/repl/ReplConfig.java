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

import static com.github.jlangch.venice.impl.VeniceClasspath.getVeniceBasePath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.github.jlangch.venice.impl.util.CommandLineArgs;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.nanojson.JsonObject;
import com.github.jlangch.venice.nanojson.JsonParser;


/**
 * REPL configuration
 * 
 * ANSI terminal colors: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html#colors	
 */
public class ReplConfig {

	private ReplConfig(
			final String configSource,
			final ColorMode colorMode,
			final String loadFile,
			final String prompt,
			final String secondaryPrompt,
			final String resultPrefix,
			final boolean syntaxHighlighting,
			final Level jlineLoglevel,
			final boolean jlineDumbTerminal,
			final boolean clearCmdHistoryOnExit,
			final Map<String,String> colors
	) {
		this.configSource = configSource;
		this.colorMode = colorMode;
		
		this.loadFile = loadFile;
		
		this.prompt = orDefault(prompt, DEFAULT_PROMPT);
		this.secondaryPrompt = orDefault(secondaryPrompt, DEFAULT_SECONDARY_PROMPT);		
		this.resultPrefix = orDefault(resultPrefix , DEFAULT_RESULT_PREFIX);
		
		this.syntaxHighlighting = syntaxHighlighting;
		
		this.jlineLoglevel = jlineLoglevel;
		this.jlineDumbTerminal = jlineDumbTerminal;
		
		this.clearCmdHistoryOnExit = clearCmdHistoryOnExit;
		
		this.colors.putAll(colors);
	}
	
	public static ReplConfig load(final CommandLineArgs cli) {
		final Map<String,String> colors = new HashMap<>();
				
		// load file
		final String loadFile = cli.switchValue("-load-file");

		try {
			final Tuple2<JsonObject,String> cfg = loadJsonConfig();
			final JsonObject jsonObj = cfg._1;
			final String jsonConfigSource = cfg._2;
			
			final String prompt = jsonObj.getString("prompt");
			final String secondaryPrompt = jsonObj.getString("secondary-prompt");
			final String resultPrefix =  jsonObj.getString("result-prefix");
			final boolean clearCmdHistoryOnExit = jsonObj.getBoolean("clear-cmd-history-on-exit", false);

			// Colors
			final ColorMode colorMode = getColorMode(cli);
			
			// Colors light mode
			JsonObject obj = (JsonObject)jsonObj.get("colors");
			if (obj != null) {
				for(String cname : COLOR_NAMES) {
					colors.put("light." + cname, StringUtil.emptyToNull(obj.getString(cname)));
				}
			}

			// Colors dark mode
			obj = (JsonObject)jsonObj.get("colors-darkmode");
			if (obj != null) {
				for(String cname : COLOR_NAMES) {
					colors.put("dark." + cname, StringUtil.emptyToNull(obj.getString(cname)));
				}
			}

			// Syntax highlighting
			boolean highlight = true;
			obj = (JsonObject)jsonObj.get("syntax-highlighting");
			if (obj != null) {
				try {
					highlight = obj.getBoolean("enabled", Boolean.TRUE);
				}
				catch(Exception ex) { }
			}
			if (colorMode == ColorMode.None) {
				highlight = false; // turn off
			}
			
			// JLine
			Level jlineLoglevel = null;
			boolean jlineDumbTerminal = false;
			obj = (JsonObject)jsonObj.get("jline");
			if (obj != null) {
				try {
					jlineLoglevel = Level.parse(obj.getString("loglevel"));
				}
				catch(Exception ex) { /* skip */ }

				try {
					jlineDumbTerminal = obj.getBoolean("dumb-terminal", Boolean.FALSE);
				}
				catch(Exception ex) {
					jlineDumbTerminal = true;
				}
			}

			return new ReplConfig(
						jsonConfigSource,
						colorMode, 
						loadFile,
						prompt,
						secondaryPrompt,
						resultPrefix,
						highlight,
						jlineLoglevel,
						jlineDumbTerminal,
						clearCmdHistoryOnExit,
						colors);
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to parse REPL json config file", ex);
		}	
	}

	public void switchColorMode(final ColorMode mode) {
		switch(mode) {
			case Light:	
				this.colorMode = ColorMode.Light;
				break;
			
			case Dark:
				this.colorMode = ColorMode.Dark;
				break;
	
			case None:
			default:
				break;
		}
	}
	
	public String getConfigSource() {
		return configSource;
	}

	public ColorMode getColorMode() {
		return colorMode;
	}

	public boolean isColorModeLight() {
		return colorMode == ColorMode.Light;
	}

	public boolean isColorModeDark() {
		return colorMode == ColorMode.Dark;
	}

	public boolean isColorModeNone() {
		return colorMode == ColorMode.None;
	}

	public String getColor(final String key) {
		switch(colorMode) {
			case Light: return lookupColor("light." + key);
			case Dark:  return lookupColor("dark." + key);
			default:    return null;
		}
	}

	public String getLoadFile() {
		return loadFile;
	}

	public String getPrompt() {
		return getColor("prompt") == null
				? prompt
				: getColor("prompt") + prompt + ReplConfig.ANSI_RESET;
	}

	public String getSecondaryPrompt() {
		return getColor("secondary-prompt") == null
				? secondaryPrompt
				: getColor("secondary-prompt") + secondaryPrompt + ReplConfig.ANSI_RESET;
	}

	public String getResultPrefix() {
		return resultPrefix;
	}
	
	public boolean isSyntaxHighlighting() {
		return syntaxHighlighting;
	}
	
	public Level getJLineLogLevel() {
		return jlineLoglevel;
	}

	public boolean isJLineDumbTerminal() {
		return jlineDumbTerminal;
	}
	
	public String getJansiVersion() {
		try (InputStream is = getClass()
								.getClassLoader()
								.getResourceAsStream("org/fusesource/jansi/jansi.properties")
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
	
	public boolean isClearCommandHistoryOnExit() {
		return clearCmdHistoryOnExit;
	}

	public static String getDefaultClasspathConfig() {
		return new ClassPathResource(getVeniceBasePath() + "repl.json")
						.getResourceAsString("UTF-8");
	}

	public static String getLauncherScriptName() {
		return System.getProperty("os.name").startsWith("Windows") ? "repl.bat" : "repl.sh";
	}

	public static String getDefaultClasspathLauncherScript() {
		return new ClassPathResource(getVeniceBasePath() + getLauncherScriptName())
						.getResourceAsString("UTF-8");
	}


	private String lookupColor(final String key) {
		return StringUtil.emptyToNull(colors.get(key));
	}
	
	private static Tuple2<JsonObject,String> loadJsonConfig() throws Exception {
		
		// [1] load from current working directory
		final File fileJson = new File("repl.json");
		if (fileJson.isFile()) {
			try (Reader reader = new FileReader(fileJson)) {
				return new Tuple2<JsonObject,String>(
							(JsonObject)JsonParser.object().from(reader),
							"file '" + fileJson.getPath() +"'");
			}
		}
		
		// [2] load custom from classpath
		try {
			final String clientConfig = new ClassPathResource("repl.json")
												.getResourceAsString("UTF-8");
			if (clientConfig != null) {
				return new Tuple2<JsonObject,String>(
							(JsonObject)JsonParser.object().from(clientConfig),
							"classpath custom 'repl.json'");
			}
		}
		catch(Exception ex) { /* ignore */ }
			
		// [3] load built-in from classpath
		final String builtinConfig = getDefaultClasspathConfig();
		if (builtinConfig != null) {
			return new Tuple2<JsonObject,String>(
						(JsonObject)JsonParser.object().from(builtinConfig),
						"built-in default 'repl.json'");
		}
		
		throw new RuntimeException("Failed to find a 'repl.json' configuration");
	}
	
	private static ColorMode getColorMode(final CommandLineArgs cli) {
		if (cli.switchPresent("-colors")) {
			return ColorMode.Light;
		}
		else if (cli.switchPresent("-colors-light")) {
			return ColorMode.Light;
		}
		else if (cli.switchPresent("-colors-lightmode")) {
			return ColorMode.Light;
		}
		else if (cli.switchPresent("-colors-dark")) {
			return ColorMode.Dark;
		}
		else if (cli.switchPresent("-colors-darkmode")) {
			return ColorMode.Dark;
		}
		else {
			return ColorMode.None;
		}
	}
	
	private static String orDefault(final String s, final String sDefault) {
		return s == null ? sDefault : s;
	}
	
	
	
	public static final String ANSI_RESET = "\u001b[0m";
	
	public static enum ColorMode { Light, Dark, None };

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

	private final String configSource;
	private ColorMode colorMode;
	private final String loadFile;
	private final String prompt;
	private final String secondaryPrompt;
	private final String resultPrefix;
	private final boolean syntaxHighlighting;
	
	private final Level jlineLoglevel;
	private final boolean jlineDumbTerminal;
	
	private final boolean clearCmdHistoryOnExit;

	private final Map<String,String> colors = new HashMap<>();
}
