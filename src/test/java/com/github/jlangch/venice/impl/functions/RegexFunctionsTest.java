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
package com.github.jlangch.venice.impl.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class RegexFunctionsTest {
	
	@Test
	public void test_regex_pattern() {
		final Venice venice = new Venice();
		
		final Pattern p = (Pattern)venice.eval("(regex/pattern \"[0-9]+\")");
		assertNotNull(p);
		assertEquals("[0-9]+", p.pattern());
	}

	@Test
	public void test_regex_matcher() {
		final Venice venice = new Venice();
		
		assertNotNull((Matcher)venice.eval(
				"(regex/matcher \"[0-9]+\" \"100\")"));

		assertNotNull((Matcher)venice.eval(
				"(let [p (regex/pattern \"[0-9]+\")]    \n" +
				"   (regex/matcher p \"100\"))            "));
	}

	@Test
	public void test_regex_find_Q() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [p (regex/pattern \"[0-9]+\")    \n" +
				"      m (regex/matcher p \"100\")]    \n" +
				"   (regex/find? m))                     ";
		
		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_regex_matches_Q() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [p (regex/pattern \"[0-9]+\")    \n" +
				"      m (regex/matcher p \"100\")]    \n" +
				"   (regex/matches? m))                  ";
		
		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_regex_find() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [m (regex/matcher \"[0-9]+\" \"672-345-456-3212\")]  \n" +
				"   [ (regex/find m)                                       \n" +
				"     (regex/find m)                                       \n" +
				"     (regex/find m)                                       \n" +
				"     (regex/find m)                                       \n" +
				"     (regex/find m) ] )                                     ";
		
		assertEquals("[672, 345, 456, 3212, null]", venice.eval(script).toString());
	}

	@Test
	public void test_regex_matches() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [groups (regex/matches                               \n" +
				"                \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\"   \n" +
				"                \"672-345-456-212\")]                     \n" +
				"   (pr-str [ (count groups)                               \n" +
				"             (first groups)                               \n" +
				"             (second groups)                              \n" +
				"             (third groups)                               \n" +
				"             (fourth groups)                               \n" +
				"             (nth groups 4) ] ))                           ";
		
		assertEquals("[5 \"672-345-456-212\" \"672\" \"345\" \"456\" \"212\"]", venice.eval(script).toString());
	}

	@Test
	public void test_regex_matches_groups_meta() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [groups (regex/matches                               \n" +
				"                \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\"   \n" +
				"                \"672-345-456-212\")]                     \n" +
				"   (pr-str [ (:start (meta groups))                       \n" +
				"             (:end (meta groups))                         \n" +
				"             (:group-count (meta groups)) ] ))              ";
		
		assertEquals("[0 15 4]", venice.eval(script).toString());
	}

	@Test
	public void test_regex_matches_group_element_meta() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [groups (regex/matches                               \n" +
				"                \"([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)\"   \n" +
				"                \"672-345-456-212\")]                     \n" +
				"   (pr-str [ [ 0                                          \n" +
				"               (:start (meta (nth groups 0)))             \n" +
				"               (:end (meta (nth groups 0))) ]             \n" +
				"             [ 1                                          \n" +
				"               (:start (meta (nth groups 1)))             \n" +
				"               (:end (meta (nth groups 1))) ]             \n" +
				"             [ 2                                          \n" +
				"               (:start (meta (nth groups 2)))             \n" +
				"               (:end (meta (nth groups 2))) ]             \n" +
				"             [ 3                                          \n" +
				"               (:start (meta (nth groups 3)))             \n" +
				"               (:end (meta (nth groups 3))) ]             \n" +
				"             [ 4                                          \n" +
				"               (:start (meta (nth groups 4)))             \n" +
				"               (:end (meta (nth groups 4))) ] ] ))          ";
		
		assertEquals("[[0 0 15] [1 0 3] [2 4 7] [3 8 11] [4 12 15]]", venice.eval(script).toString());
	}

	
	@Test
	public void test_regex_group() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [p (regex/pattern \"([0-9]+)(.*)\")      \n" +
				"      m (regex/matcher p \"100abc\")]         \n" +
				"   (if (regex/matches? m)                     \n" +
				"      [(regex/group m 1) (regex/group m 2)]   \n" +
				"      []))                                      ";
		
		assertEquals("[100, abc]", venice.eval(script).toString());
	}

	@Test
	public void test_regex_groupcount() {
		final Venice venice = new Venice();
		
		final String script =
				"(let [p (regex/pattern \"([0-9]+)(.*)\")  \n" +
				"      m (regex/matcher p \"100abc\")]     \n" +
				"   (regex/count m))                         ";
		
		assertEquals(2L, venice.eval(script));
	}

}
