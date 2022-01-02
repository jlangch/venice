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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class RingModuleTest {

	@Test
	public void test_uri_filter() {
		assertTrue(test_filter("/", "/"));
		assertFalse(test_filter("/", "/a"));

		assertTrue(test_filter("/**", "/"));
		assertTrue(test_filter("/**", "/a"));
		assertTrue(test_filter("/**", "/a/"));
		assertTrue(test_filter("/**", "/a/b"));
		assertTrue(test_filter("/**", "/a/b/"));
		assertTrue(test_filter("/**", "/a/b/c"));

		assertTrue(test_filter("/x/**", "/x/"));
		assertTrue(test_filter("/x/**", "/x/a"));
		assertTrue(test_filter("/x/**", "/x/a/"));
		assertTrue(test_filter("/x/**", "/x/a/b"));
		assertTrue(test_filter("/x/**", "/x/a/b/"));
		assertTrue(test_filter("/x/**", "/x/a/b/c"));
		assertFalse(test_filter("/x/**", "/"));
		assertFalse(test_filter("/x/**", "/x"));
		assertFalse(test_filter("/x/**", "/a"));
		assertFalse(test_filter("/x/**", "/a/"));
		assertFalse(test_filter("/x/**", "/a/b"));

		assertFalse(test_filter("/x/**/z", "/x/z"));
		assertTrue(test_filter("/x/**/z", "/x/a/z"));
		assertTrue(test_filter("/x/**/z", "/x/a/b/z"));
		assertTrue(test_filter("/x/**/z", "/x/a/b/c/z"));
		assertFalse(test_filter("/x/**/z", "/"));
		assertFalse(test_filter("/x/**/z", "/x"));
		assertFalse(test_filter("/x/**/z", "/z"));
		assertFalse(test_filter("/x/**/z", "/a"));
		assertFalse(test_filter("/x/**/z", "/a/"));
		assertFalse(test_filter("/x/**/z", "/a/b"));
		assertFalse(test_filter("/x/**/z", "/x/a/b/c/"));
		assertFalse(test_filter("/x/**/z", "/x/a/b/c/y"));

		assertFalse(test_filter("/*.png", "/"));
		assertTrue(test_filter("/*.png", "/x.png"));
		assertTrue(test_filter("/a/*.png", "/a/x.png"));
		assertTrue(test_filter("/a/b/*.png", "/a/b/x.png"));
		assertTrue(test_filter("/a/b/c/*.png", "/a/b/c/x.png"));

		assertFalse(test_filter("/**/*.png", "/x.png"));
		assertTrue(test_filter("/**/*.png", "/a/x.png"));
		assertTrue(test_filter("/**/*.png", "/a/b/x.png"));
		assertTrue(test_filter("/**/*.png", "/a/b/c/x.png"));

		assertTrue(test_filter("/**/test/*.png", "/a/test/x.png"));
		assertTrue(test_filter("/**/test/*.png", "/a/a/test/x.png"));

		assertTrue(test_filter("/**/test/**/*.png", "/a/test/b/x.png"));
		assertTrue(test_filter("/**/test/**/*.png", "/a/a/test/b/b/x.png"));

		// special chars
		assertTrue(test_filter("/a/?.png", "/a/?.png"));
		assertTrue(test_filter("/a/*.png", "/a/?.png"));
		assertTrue(test_filter("/a/(x).png", "/a/(x).png"));
		assertTrue(test_filter("/a/*.png", "/a/(x).png"));
		assertTrue(test_filter("/a/a{3}.png", "/a/a{3}.png"));
		assertTrue(test_filter("/a/*.png", "/a/a{3}.png"));
		assertTrue(test_filter("/a/a[3].png", "/a/a[3].png"));
		assertTrue(test_filter("/a/*.png", "/a/a[3].png"));
	}

	@Test
	public void test_uri_filter_params() {
		assertTrue(test_params("/a/:id", "/a/5000"));
		assertTrue(test_params("/a/:id", "/a/XYZ"));
		assertTrue(test_params("/a/:id", "/a/XYZ500_"));
		
		assertTrue(test_params("/a/b/:id", "/a/b/5000"));
		assertTrue(test_params("/a/b/:id", "/a/b/XYZ"));
		assertTrue(test_params("/a/b/:id", "/a/b/XYZ500_"));
		
		assertTrue(test_params("/a/b/:id/c", "/a/b/5000/c"));
		assertTrue(test_params("/a/b/:id/c", "/a/b/XYZ/c"));
		assertTrue(test_params("/a/b/:id/c", "/a/b/XYZ500_/c"));

		
		assertTrue(test_params("/a/:something_00", "/a/5000"));
		assertTrue(test_params("/a/:something_00", "/a/XYZ"));
		assertTrue(test_params("/a/:something_00", "/a/XYZ500_"));
		
		assertTrue(test_params("/a/b/:something_00", "/a/b/5000"));
		assertTrue(test_params("/a/b/:something_00", "/a/b/XYZ"));
		assertTrue(test_params("/a/b/:something_00", "/a/b/XYZ500_"));
		
		assertTrue(test_params("/a/b/:something_00/c", "/a/b/5000/c"));
		assertTrue(test_params("/a/b/:something_00/c", "/a/b/XYZ/c"));
		assertTrue(test_params("/a/b/:something_00/c", "/a/b/XYZ500_/c"));
	}
	
	private static boolean test_filter(
			final String filter, 
			final String uri
	) {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :ring)                                    \n" +
				"   (let [re (ring/uri-filter-regex  \"" + filter + "\")]  \n" + 
				"      (-> (regex/matcher re uri)                          \n" + 
				"          (regex/matches?)))                              \n" + 
				")";

		return (Boolean)venice.eval(script, Parameters.of("uri", uri));
	}
	
	private static boolean test_params(
			final String filter, 
			final String uri
	) {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :ring)                                    \n" +
				"   (let [re (ring/uri-params-regex  \"" + filter + "\")]  \n" + 
				"      (-> (regex/matcher re uri)                          \n" + 
				"          (regex/matches?)))                              \n" + 
				")";

		return (Boolean)venice.eval(script, Parameters.of("uri", uri));
	}
}
