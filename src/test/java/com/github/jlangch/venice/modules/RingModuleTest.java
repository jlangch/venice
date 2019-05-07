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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class RingModuleTest {

	@Test
	public void test_uri_filter() {
		assertTrue(test("/", "/"));
		assertFalse(test("/", "/a"));

		assertTrue(test("/**", "/"));
		assertTrue(test("/**", "/a"));
		assertTrue(test("/**", "/a/"));
		assertTrue(test("/**", "/a/b"));
		assertTrue(test("/**", "/a/b/"));
		assertTrue(test("/**", "/a/b/c"));

		assertTrue(test("/x/**", "/x/"));
		assertTrue(test("/x/**", "/x/a"));
		assertTrue(test("/x/**", "/x/a/"));
		assertTrue(test("/x/**", "/x/a/b"));
		assertTrue(test("/x/**", "/x/a/b/"));
		assertTrue(test("/x/**", "/x/a/b/c"));
		assertFalse(test("/x/**", "/"));
		assertFalse(test("/x/**", "/x"));
		assertFalse(test("/x/**", "/a"));
		assertFalse(test("/x/**", "/a/"));
		assertFalse(test("/x/**", "/a/b"));

		assertFalse(test("/x/**/z", "/x/z"));
		assertTrue(test("/x/**/z", "/x/a/z"));
		assertTrue(test("/x/**/z", "/x/a/b/z"));
		assertTrue(test("/x/**/z", "/x/a/b/c/z"));
		assertFalse(test("/x/**/z", "/"));
		assertFalse(test("/x/**/z", "/x"));
		assertFalse(test("/x/**/z", "/z"));
		assertFalse(test("/x/**/z", "/a"));
		assertFalse(test("/x/**/z", "/a/"));
		assertFalse(test("/x/**/z", "/a/b"));
		assertFalse(test("/x/**/z", "/x/a/b/c/"));
		assertFalse(test("/x/**/z", "/x/a/b/c/y"));

		assertFalse(test("/*.png", "/"));
		assertTrue(test("/*.png", "/x.png"));
		assertTrue(test("/a/*.png", "/a/x.png"));
		assertTrue(test("/a/b/*.png", "/a/b/x.png"));
		assertTrue(test("/a/b/c/*.png", "/a/b/c/x.png"));

		assertFalse(test("/**/*.png", "/x.png"));
		assertTrue(test("/**/*.png", "/a/x.png"));
		assertTrue(test("/**/*.png", "/a/b/x.png"));
		assertTrue(test("/**/*.png", "/a/b/c/x.png"));

		assertTrue(test("/**/test/*.png", "/a/test/x.png"));
		assertTrue(test("/**/test/*.png", "/a/a/test/x.png"));

		assertTrue(test("/**/test/**/*.png", "/a/test/b/x.png"));
		assertTrue(test("/**/test/**/*.png", "/a/a/test/b/b/x.png"));

		// special chars
		assertTrue(test("/a/?.png", "/a/?.png"));
		assertTrue(test("/a/*.png", "/a/?.png"));
		assertTrue(test("/a/(x).png", "/a/(x).png"));
		assertTrue(test("/a/*.png", "/a/(x).png"));
		assertTrue(test("/a/a{3}.png", "/a/a{3}.png"));
		assertTrue(test("/a/*.png", "/a/a{3}.png"));
		assertTrue(test("/a/a[3].png", "/a/a[3].png"));
		assertTrue(test("/a/*.png", "/a/a[3].png"));
	}
	
	private static boolean test(
			final String filter, 
			final String uri
	) {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :ring)                             \n" +
				"   (let [re (ring/uri-regex  \"" + filter + "\")]  \n" + 
				"      (-> (regex/matcher re uri)                   \n" + 
				"                       (regex/matches?)))          \n" + 
				")";

		return (Boolean)venice.eval(script, Parameters.of("uri", uri));
	}
}
