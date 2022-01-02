/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.javainterop;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.LoadPaths;


/**
 * Factory for creating <code>ILoadPaths</code> objects. 
 * 
 * @see com.github.jlangch.venice.javainterop.ILoadPaths
 * @author juerg
 */
public class LoadPathsFactory {

	/**
	 * Creates a load path that can load files from everywhere
	 * in the filesystem.
	 * 
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths acceptAll() {
		return LoadPaths.of(null, true);
	}

	/**
	 * Creates a load path that rejects to load any file 
	 * 
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths rejectAll() {
		return LoadPaths.of(null, false);
	}

	/**
	 * Creates a load path that allows loading files only from the
	 * specified load paths. The file paths to load a file must
	 * be relative to a load path.
	 * 
	 * @param paths a list of absolute directories
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths of(final List<File> paths) {
		return LoadPaths.of(paths, false);
	}

	/**
	 * Creates a load path that allows loading files from the
	 * specified load paths. The file paths to load a file must
	 * be relative to a load path.
	 * If 'unlimitedAccess' is <code>true</code> files are allowed
	 * to be loaded from outside the load paths.
	 * 
	 * @param paths a list of absolute directories
	 * @param unlimitedAccess If <code>true</code> allow files to be
	 *                        loaded from outside the load paths.
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths of(
			final List<File> paths, 
			final boolean unlimitedAccess
	) {
		return LoadPaths.of(paths, unlimitedAccess);
	}

	/**
	 * Creates a load path from semi-colon delimited list of 
	 * paths. The file paths to load a file must
	 * be relative to a load path.
	 * 
	 * @param loadPaths a semi-colon delimited list of paths
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths parseDelimitedLoadPath(final String loadPaths) {
		return parseDelimitedLoadPath(loadPaths, false);
	}
	
	/**
	 * Creates a load path from semi-colon delimited list of 
	 * paths. The file paths to load a file must
	 * be relative to a load path.
	 * If 'unlimitedAccess' is <code>true</code> files are allowed
	 * to be loaded from outside the load paths.
	 * 
	 * @param loadPaths a semi-colon delimited list of paths
	 * @param unlimitedAccess If <code>true</code> allow files to be
	 *                        loaded from outside the load paths.
	 * @return an ILoadPaths 
	 */
	public static ILoadPaths parseDelimitedLoadPath(
			final String loadPaths, 
			final boolean unlimitedAccess
	) {
		if (loadPaths == null) {
			return of(null, unlimitedAccess);
		}
		else {
			return LoadPaths.of(
					Arrays.stream(loadPaths.trim().split(";"))
						  .map(p -> StringUtil.trimToNull(p))
						  .filter(p -> p != null)
						  .map(p -> new File(p))
						  .collect(Collectors.toList()),
					unlimitedAccess);
		}
	}

}
