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


public class ConfigModuleTest {

	@Test
	public void test_thread_ks_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :config)                                  \n" +
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java\" nil))))               \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java\" \"java\"))))          \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java\" \"java.\"))))         \n" + 
				"                                                          \n" +
				"   (assert (= '(:home)                                    \n" +
				"              (config/->ks \"java\" \"java.home\"))))     \n" + 
				"                                                          \n" +
				"   (assert (= '(:home :vm)                                \n" +
				"              (config/->ks \"java\" \"java.home.vm\"))))    "; 

		venice.eval(script);
	}

	@Test
	public void test_thread_ks_2() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :config)                                  \n" +
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"\" nil))))                   \n" + 
				"                                                          \n" +
				"   (assert (= '(:java)                                    \n" +
				"              (config/->ks \"\" \"java\"))))              \n" + 
				"                                                          \n" +
				"   (assert (= '(:java)                                    \n" +
				"              (config/->ks \"\" \"java.\"))))             \n" + 
				"                                                          \n" +
				"   (assert (= '(:java :home)                              \n" +
				"              (config/->ks \"\" \"java.home\"))))         \n" + 
				"                                                          \n" +
				"   (assert (= '(:java :home :vm)                          \n" +
				"              (config/->ks \"\" \"java.home.vm\"))))        "; 

		venice.eval(script);
	}

	@Test
	public void test_thread_ks_3() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :config)                                  \n" +
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks nil nil))))                    \n" + 
				"                                                          \n" +
				"   (assert (= '(:java)                                    \n" +
				"              (config/->ks nil \"java\"))))               \n" + 
				"                                                          \n" +
				"   (assert (= '(:java)                                    \n" +
				"              (config/->ks nil \"java.\"))))              \n" + 
				"                                                          \n" +
				"   (assert (= '(:java :home)                              \n" +
				"              (config/->ks nil \"java.home\"))))          \n" + 
				"                                                          \n" +
				"   (assert (= '(:java :home :vm)                          \n" +
				"              (config/->ks nil \"java.home.vm\"))))        "; 

		venice.eval(script);
	}

	@Test
	public void test_thread_ks_4() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :config)                                  \n" +
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"kava\" \"java\"))))          \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"kava\" \"java.\"))))         \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"kava\" \"java.home\"))))     \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"kava\" \"java.home.vm\"))))    "; 
		venice.eval(script);
	}

	@Test
	public void test_thread_ks_5() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                       \n" +
				"   (load-module :config)                                  \n" +
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java.\" nil))))              \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java.\" \"java\"))))         \n" + 
				"                                                          \n" +
				"   (assert (= nil                                         \n" +
				"              (config/->ks \"java.\" \"java.\"))))        \n" + 
				"                                                          \n" +
				"   (assert (= '(:home)                                    \n" +
				"              (config/->ks \"java.\" \"java.home\"))))    \n" + 
				"                                                          \n" +
				"   (assert (= '(:home :vm)                                \n" +
				"              (config/->ks \"java.\" \"java.home.vm\"))))    "; 

		venice.eval(script);
	}
}
