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
package com.github.jlangch.venice.impl.repl;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.util.CommandLineArgs;


/**
 * REPL configuration
 * 
 * ANSI terminal colors: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html#colors	
 */
public class ReplConfig {

	private ReplConfig(final Map<String,String> config) {
		this.config = config;
	}
	
	public static ReplConfig load(final CommandLineArgs cli) {
		final Map<String,String> config = new HashMap<>();
		
		// use colors
		config.put("colors.use", cli.switchPresent("-colors") ? "true" : "false");
				
		try {
			final JSONObject jsonObj = loadJsonConfig();
			final JSONObject colObj = (JSONObject)jsonObj.get("colors");
			if (colObj != null) {
				for(String cname : Arrays.asList("result", "stdout", "error", "interrupt", "prompt")) {
					config.put("colors." + cname, emptyToNull((String)colObj.get(cname)));
				}
			}

			return new ReplConfig(config);
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to parse REPL json config file", ex);
		}	
	}
	
	public String get(final String key) {
		return useColors() ? emptyToNull(config.get(key)) : null;
	}
	
	public String getOrDefault(final String key, final String defaultValue) {
		return useColors() 
					? emptyToNull(config.getOrDefault(key, defaultValue)) 
					: null;
	}

	public boolean useColors() {
		return "true".equals(config.get("colors.use"));
	}

	public String getPrompt() {
		return !useColors() || get("colors.prompt") == null
				? PROMPT
				: get("colors.prompt") + PROMPT + ReplConfig.ANSI_RESET;
	}

	private static JSONObject loadJsonConfig() throws Exception {
		final File fileJson = new File("repl.json");

		final JSONParser parser = new JSONParser();

		if (fileJson.isFile()) {
			System.out.println("Loading REPL config from " + fileJson + "...");
			return (JSONObject)parser.parse(new FileReader(fileJson));
		}
		else {
			final String cpathJson = new ClassPathResource("com/github/jlangch/venice/repl.json")
											.getResourceAsString("UTF-8");
			return (JSONObject)parser.parse(cpathJson);
		}
	}
	
	private static String emptyToNull(final String s) {
		return s == null || s.isEmpty() ? null : s;
	}
	

	public static final String ANSI_RESET = "\u001b[0m";
	public static final String PROMPT = "venice> ";

	private final Map<String,String> config;
}
