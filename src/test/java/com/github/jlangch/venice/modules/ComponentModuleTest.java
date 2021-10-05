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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


public class ComponentModuleTest {

	@Test
	public void test_1() {
		final Venice venice = new Venice();

		final String script =
				"(do                                                                  \n"
				+ "  (load-module :component)                                         \n"
				+ "                                                                   \n"
				+ "  (deftype :server [port       :long                               \n"
				+ "                    components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \":server started\") this)           \n"
				+ "       (stop [this] (println \":server stopped\") this)            \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (deftype :database [user       :string                           \n"
				+ "                      password   :string                           \n"
				+ "                      components :map ]                            \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \":database started\") this)         \n"
				+ "       (stop [this] (println \":database stopped\") this)          \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :server (server. 4600 {})                               \n"
				+ "           :store  (database. \"foo\" \"123\" {}))                 \n"
				+ "        (component/system-using {:server [:store]})))              \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (-> (create-system)                                            \n"
				+ "        (component/start)                                          \n"
				+ "        (component/stop))))                                          "; 

		assertEquals(
			":database started\n" +
			":server started\n" +
			":server stopped\n" +
			":database stopped\n",
			venice.eval(script));
	}

}
