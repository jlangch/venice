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
package com.github.jlangch.venice.impl;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.github.jlangch.venice.impl.util.ClassPathResource;


/**
 * REPL configuration
 * 
 * ANSI terminal colors: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html#colors	
 */
public class ReplConfig {

	private ReplConfig(final Map<String,String> config) {
		this.config = config;
	}
	
	@SuppressWarnings("unchecked")
	public static ReplConfig load(final boolean useColors) {
		final Map<String,String> config = new HashMap<>();
		
		// defaults
		config.put("colors.use", useColors ? "true" : "false");
		config.put("colors.result", "\u001b[38;5;20m");
		config.put("colors.stdout", "\u001b[38;5;243m");
		config.put("colors.error", "\u001b[38;5;202m");
		config.put("colors.interrupt", "\u001b[48;5;196m\u001b[38;5;15m");
				
		try {
			final String cpathJson = new ClassPathResource("com/github/jlangch/venice/repl.json")
											.getResourceAsString("UTF-8");

			final File fileJson = new File("repl.json");

			final JSONParser parser = new JSONParser();
			
			final List<JSONObject> jsonObjs = new ArrayList<>();
			jsonObjs.add((JSONObject)parser.parse(cpathJson));
			if (fileJson.isFile()) {
				System.out.println("Loading REPL config from " + fileJson + "...");
				jsonObjs.add((JSONObject)parser.parse(new FileReader(fileJson)));
			}
			
			for(JSONObject jsonObj: jsonObjs) {
				JSONObject colObj = (JSONObject)jsonObj.get("colors");
				if (colObj != null) {
					for(String cname : Arrays.asList("result", "stdout", "error", "interrupt")) {
						config.put(
								"colors." + cname, 
								(String)colObj.getOrDefault(cname, config.get("colors." + cname)));
					}
				}
			}			
			return new ReplConfig(config);
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to parse REPL json config file", ex);
		}	
	}
	
	public String get(final String key) {
		return config.get(key);
	}
	
	public String getOrDefault(final String key, final String defaultValue) {
		return config.getOrDefault(key, defaultValue);
	}

	public boolean useColors() {
		return "true".equals(config.get("colors.use"));
	}


	private final Map<String,String> config;
}
