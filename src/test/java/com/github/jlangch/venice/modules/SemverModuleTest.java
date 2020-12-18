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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class SemverModuleTest {

	@Test
	public void test_version_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3-snapshot+b1\")]   \n" + 
				"      (assert (== 1 (:major v)))                   \n" + 
				"      (assert (== 2 (:minor v)))                   \n" + 
				"      (assert (== 3 (:patch v)))                   \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== \"snapshot\" (:pre-release v)))  \n" + 
				"      (assert (== \"b1\" (:meta-data v))))         \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_version_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3-snapshot\")]      \n" + 
				"      (assert (== 1 (:major v)))                   \n" + 
				"      (assert (== 2 (:minor v)))                   \n" + 
				"      (assert (== 3 (:patch v)))                   \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== \"snapshot\" (:pre-release v)))  \n" + 
				"      (assert (== nil (:meta-data v))))            \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_version_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3\")]               \n" + 
				"      (assert (== 1 (:major v)))                   \n" + 
				"      (assert (== 2 (:minor v)))                   \n" + 
				"      (assert (== 3 (:patch v)))                   \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== nil (:pre-release v)))           \n" + 
				"      (assert (== nil (:meta-data v))))            \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_version_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2\")]                 \n" + 
				"      (assert (== nil (:major v)))                 \n" + 
				"      (assert (== nil (:minor v)))                 \n" + 
				"      (assert (== nil (:patch v)))                 \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== nil (:pre-release v)))           \n" + 
				"      (assert (== nil (:meta-data v))))            \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_version_5() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1\")]                   \n" + 
				"      (assert (== nil (:major v)))                 \n" + 
				"      (assert (== nil (:minor v)))                 \n" + 
				"      (assert (== nil (:patch v)))                 \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== nil (:pre-release v)))           \n" + 
				"      (assert (== nil (:meta-data v))))            \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_version_6() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"\")]                    \n" + 
				"      (assert (== nil (:major v)))                 \n" + 
				"      (assert (== nil (:minor v)))                 \n" + 
				"      (assert (== nil (:patch v)))                 \n" + 
				"      (assert (== nil (:revision v)))              \n" + 
				"      (assert (== nil (:pre-release v)))           \n" + 
				"      (assert (== nil (:meta-data v))))            \n" + 
				") ";

		venice.eval(script);
	}

	@Test
	public void test_valid_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3-snapshot+b1\")]   \n" + 
				"      (assert (semver/valid? v))))                 \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_valid_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3-snapshot\")]      \n" + 
				"      (assert (semver/valid? v))))                 \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_valid_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3\")]               \n" + 
				"      (assert (semver/valid? v))))                 \n" + 
				") ";

		venice.eval(script);
	}

	@Test
	public void test_invalid_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2.3s-snapshot+b1\")]  \n" + 
				"      (assert (not (semver/valid? v)))))           \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_invalid_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.a.3-snapshot\")]      \n" + 
				"      (assert (not (semver/valid? v)))))           \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_invalid_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"a.2.3\")]               \n" + 
				"      (assert (not (semver/valid? v)))))           \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_invalid_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"1.2\")]                 \n" + 
				"      (assert (not (semver/valid? v)))))           \n" + 
				") ";

		venice.eval(script);
	}
	
	@Test
	public void test_invalid_5() {
		final Venice venice = new Venice();

		final String script =
				"(do                               \n" +
				"   (load-module :semver)          \n" +
				"                                  \n" +
				"   (-<> (semver/parse \"1\")      \n" + 
				"        (semver/valid? <>)        \n" + 
				"        (not <>)                  \n" + 
				"        (assert <>)))               ";

		venice.eval(script);
	}
	
	@Test
	public void test_invalid_6() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (let [v (semver/parse \"\")]                    \n" + 
				"      (assert (not (semver/valid? v)))))           \n" + 
				") ";

		venice.eval(script);
	}

	@Test
	public void test_valid_format_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (semver/valid-format? \"1.2.3-snapshot+b1\")))    "; 

		venice.eval(script);
	}
	
	@Test
	public void test_valid_format_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (semver/valid-format? \"1.2.3-snapshot\")))       "; 

		venice.eval(script);
	}
	
	@Test
	public void test_valid_format_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (semver/valid-format? \"1.2.3\")))                "; 

		venice.eval(script);
	}
	
	@Test
	public void test_valid_format_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (not (semver/valid-format? \"1.2\"))))            "; 

		venice.eval(script);
	}
	
	@Test
	public void test_valid_format_5() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (not (semver/valid-format? \"1\"))))              "; 

		venice.eval(script);
	}
	
	@Test
	public void test_valid_format_6() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                        \n" +
				"   (load-module :semver)                                   \n" +
				"                                                           \n" +
				"   (assert (not (semver/valid-format? \"\")))   )            "; 

		venice.eval(script);
	}

	@Test
	public void test_newer_1a() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/newer?                                  \n" +
				"      (semver/version \"1.2.4\")                   \n" + 
				"      (semver/version \"1.2.3\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_newer_1b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/newer? \"1.2.4\" \"1.2.3\")             \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_newer_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/newer?                                  \n" +
				"      (semver/version \"1.3.3\")                   \n" + 
				"      (semver/version \"1.2.9\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_newer_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/newer?                                  \n" +
				"      (semver/version \"2.2.3\")                   \n" + 
				"      (semver/version \"1.9.9\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_newer_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/newer?                                  \n" +
				"      (semver/parse \"1.2.4-snapshot+b1\")         \n" + 
				"      (semver/parse \"1.2.3-snapshot+b3\"))        \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_equal_1a() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/equal?                                  \n" +
				"      (semver/version \"1.2.3\")                   \n" + 
				"      (semver/version \"1.2.3\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_equal_1b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/equal? \"1.2.3\" \"1.2.3\")             \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_equal_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/equal?                                  \n" +
				"      (semver/parse \"1.2.3-snapshot\")            \n" + 
				"      (semver/parse \"1.2.3-snapshot\"))           \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_equal_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/equal?                                  \n" +
				"      (semver/parse \"1.2.3-snapshot+b1\")         \n" + 
				"      (semver/parse \"1.2.3-snapshot+b1\"))        \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_older_1a() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/older?                                  \n" +
				"      (semver/version \"1.2.3\")                   \n" + 
				"      (semver/version \"1.2.4\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_older_1b() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/older? \"1.2.3\" \"1.2.4\")             \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_older_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/older?                                  \n" +
				"      (semver/version \"1.2.9\")                   \n" + 
				"      (semver/version \"1.3.3\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_older_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/older?                                  \n" +
				"      (semver/version \"1.9.9\")                   \n" + 
				"      (semver/version \"2.2.3\"))                  \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

	@Test
	public void test_older_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                \n" +
				"   (load-module :semver)                           \n" +
				"                                                   \n" +
				"   (semver/older?                                  \n" +
				"      (semver/parse \"1.2.3-snapshot+b3\")         \n" + 
				"      (semver/parse \"1.2.4-snapshot+b1\"))        \n" + 
				") ";

		assertTrue((Boolean)venice.eval(script));
	}

}
