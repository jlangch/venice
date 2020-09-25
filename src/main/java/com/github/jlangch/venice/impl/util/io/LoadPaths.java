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
package com.github.jlangch.venice.impl.util.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.StringUtil;


public class LoadPaths {
	
	public static List<String> parseFromString(final String path) {
		final String loadPath = StringUtil.trimToNull(path);
		return loadPath == null
				? new ArrayList<>()
				: sanitize(Arrays.asList(loadPath.split(";")));
	}

	public static List<String> sanitize(final List<String> paths) {
		if (paths == null) {
			return new ArrayList<>();
		}
		else {
			return paths
					.stream()
					.map(p -> StringUtil.trimToNull(p))
					.filter(p -> p != null)					
					.map(p -> makeCanocicalPath(p))
					.collect(Collectors.toList());
		}
	}

	public static VncList toVncList(final List<String> paths) {
		return paths == null || paths.isEmpty()
				? VncList.empty()
				: VncList.ofList(paths
									.stream()
									.map(p -> new VncString(p))
									.collect(Collectors.toList()));
	}
	
	
	private static String makeCanocicalPath(final String path) {
		try {
			return new File(path).getCanonicalPath();
		}
		catch(IOException ex) {
			throw new RuntimeException(
					"Cannot make a canonical path form \"" + path + "\"");
		}
	}

}
