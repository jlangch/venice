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

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class EsrModuleTest {

	@Test
	public void test_checksum_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                                        \n" +
				"   (load-module :esr)                                                                      \n" +
				"   (assert (= (char \"8\") (esr/modulo-10-checksum \"150001 1234567890123456 00 0 1\")))   \n" +
				"   (assert (= (char \"9\") (esr/modulo-10-checksum \"150001 1234567890123456 00 1 1\")))   \n" +
				"   (assert (= (char \"5\") (esr/modulo-10-checksum \"150001 1234567890123456 00 2 1\")))   \n" +
				"   (assert (= (char \"4\") (esr/modulo-10-checksum \"150001 1234567890123456 00 3 1\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_checksum_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                                    \n" +
				"   (load-module :esr)                                                                  \n" +
				"   (assert (= (char \"8\") (esr/modulo-10-checksum \"15000112345678901234560001\")))   \n" +
				"   (assert (= (char \"9\") (esr/modulo-10-checksum \"15000112345678901234560011\")))   \n" +
				"   (assert (= (char \"5\") (esr/modulo-10-checksum \"15000112345678901234560021\")))   \n" +
				"   (assert (= (char \"4\") (esr/modulo-10-checksum \"15000112345678901234560031\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_format() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                            \n" +
				"   (load-module :esr)                                          \n" +
				"   (assert (= \"15 00011 23456 78901 23456 00018\"             \n" +
				"              (esr/format \"150001123456789012345600018\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_normalize_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (assert (= \"150001123456789012345600018\"                      \n" +
				"              (esr/normalize \"15 00011 23456 78901 23456 00018\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_normalize_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (assert (= \"150001123456789012345600018\"                      \n" +
				"              (esr/normalize \"150001123456789012345600018\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_create_1() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (assert (= \"150001123456789012345600018\"                      \n" +
				"              (esr/create \"150001\" \"12345678901234560001\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_create_2() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (assert (= \"158888000000000001234567892\"                      \n" +
				"              (esr/create \"158888\" \"00000000000123456789\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_create_3() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (assert (= \"158888000000000001234567892\"                      \n" +
				"              (esr/create \"158888\" \"123456789\"))))"; 

		venice.eval(script);
	}

	@Test
	public void test_parse() {
		final Venice venice = new Venice();

		final String script = 
				"(do                                                                \n" +
				"   (load-module :esr)                                              \n" +
				"   (let [esr (esr/parse \"158888000000000001234567892\")]          \n" +
				"     (assert (= \"158888\" (:identification-nr esr)))              \n" +
				"     (assert (= \"123456789\" (:invoice-nr esr)))                  \n" +
				"     (assert (= \"2\" (:checksum esr)))))"; 

		venice.eval(script);
	}

}
