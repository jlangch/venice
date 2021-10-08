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
	public void test_base_single_component() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
				+ "  (load-module :component)                                         \n"
				+ "                                                                   \n"
				+ "  (deftype :server [port       :long                               \n"
				+ "                    components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \":server started\") this)           \n"
				+ "       (stop [this] (println \":server stopped\") this)            \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :server (server. 4600 {}))                              \n"
				+ "        (component/system-using {:server []})))                    \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (-> (create-system)                                            \n"
				+ "        (component/start)                                          \n"
				+ "        (component/stop))))                                          "; 

		assertEquals(
			":server started\n" +
			":server stopped\n",
			venice.eval(script));
	}

	@Test
	public void test_base() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
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

	@Test
	public void test_base_mixed_component_without_dependency() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
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
				+ "  (deftype :logger [components :map ]                              \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \":logger started\") this)           \n"
				+ "       (stop [this] (println \":logger stopped\") this)            \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :server (server. 4600 {})                               \n"
				+ "           :store  (database. \"foo\" \"123\" {})                  \n"
				+ "           :logger (logger. {}))                                   \n"
				+ "        (component/system-using {:server [:store]                  \n"
				+ "                                 :logger []})))                    \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (-> (create-system)                                            \n"
				+ "        (component/start)                                          \n"
				+ "        (component/stop))))                                          "; 

		assertEquals(
			":database started\n" +
			":server started\n" +
			":logger started\n" +
			":logger stopped\n" +
			":server stopped\n" +
			":database stopped\n",
			venice.eval(script));
	}

	@Test
	public void test_component_info() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
				+ "  (load-module :component)                                         \n"
				+ "                                                                   \n"
				+ "  (deftype :server [port       :long                               \n"
				+ "                    components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \"~(id this) started\") this)        \n"
				+ "       (stop [this] (println \"~(id this) stopped\") this)         \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (deftype :database [user       :string                           \n"
				+ "                      password   :string                           \n"
				+ "                      components :map ]                            \n"
				+ "     component/Component                                           \n"
				+ "       (start [this] (println \"~(id this) started\") this)        \n"
				+ "       (stop [this] (println \"~(id this) stopped\") this)         \n"
				+ "       (inject [this deps] (assoc this :components deps)))         \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :server (server. 4600 {})                               \n"
				+ "           :store (database. \"foo\" \"123\" {}))                  \n"
				+ "        (component/system-using {:server [:store]})))              \n"
				+ "                                                                   \n"
				+ "  (defn- id [this]                                                 \n"
				+ "    (-> this :components :component-info :id))                     \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (-> (create-system)                                            \n"
				+ "        (component/start)                                          \n"
				+ "        (component/stop))))                                          "; 

		assertEquals(
			":store started\n" +
			":server started\n" +
			":server stopped\n" +
			":store stopped\n",
			venice.eval(script));
	}

	@Test
	public void test_component_with_config() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
				+ "  (load-module :component)                                         \n"
				+ "                                                                   \n"
				+ "  (deftype :config [cfg :map                                       \n"
				+ "                    components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this]                                               \n"
				+ "          (println \"~(id this) started\") this)                   \n"
				+ "       (stop [this]                                                \n"
				+ "         (println \"~(id this) stopped\") this)                    \n"
				+ "       (inject [this deps]                                         \n"
				+ "         (assoc this :components deps)))                           \n"
				+ "                                                                   \n"
				+ "  (deftype :server [components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this]                                               \n"
				+ "          (let [port (get-cfg this :server :port)]                 \n"
				+ "            (println \"~(id this) started at port ~{port}\") this))\n"
				+ "       (stop [this]                                                \n"
				+ "          (println \"~(id this) stopped\") this)                   \n"
				+ "       (inject [this deps]                                         \n"
				+ "          (assoc this :components deps)))                          \n"
				+ "                                                                   \n"
				+ "  (deftype :database [components :map]                             \n"
				+ "     component/Component                                           \n"
				+ "       (start [this]                                               \n"
				+ "          (let [user (get-cfg this :db :user)]                     \n"
				+ "            (println \"~(id this) started (user: ~{user})\") this))\n"
				+ "       (stop [this]                                                \n"
				+ "          (println \"~(id this) stopped\") this)                   \n"
				+ "       (inject [this deps]                                         \n"
				+ "          (assoc this :components deps)))                          \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :config (config. {:server {:port 4600}                  \n"
				+ "                             :db {:user \"foo\" :pwd \"123\"}}     \n"
				+ "                            {})                                    \n"
				+ "           :server (server. {})                                    \n"
				+ "           :store (database. {}))                                  \n"
				+ "        (component/system-using                                    \n"
				+ "           {:server [:store :config]                               \n"
				+ "            :store  [:config]})))                                  \n"
				+ "                                                                   \n"
				+ "  (defn- id [this]                                                 \n"
				+ "    (-> this :components :component-info :id))                     \n"
				+ "                                                                   \n"
				+ "  (defn- get-cfg [this & ks]                                       \n"
				+ "    (-> this :components :config :cfg (get-in ks)))                \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (-> (create-system)                                            \n"
				+ "        (component/start)                                          \n"
				+ "        (component/stop))))                                          "; 

		assertEquals(
			":config started\n" +
			":store started (user: foo)\n"  +
			":server started at port 4600\n" +
			":server stopped\n" +
			":store stopped\n"  +
			":config stopped\n",
			venice.eval(script));
	}

	@Test
	public void test_double_start() {
		final Venice venice = new Venice();

		final String script =
				  "(do                                                                \n"
				+ "  (load-module :component)                                         \n"
				+ "                                                                   \n"
				+ "  (deftype :server [port       :long                               \n"
				+ "                    components :map]                               \n"
				+ "     component/Component                                           \n"
				+ "       (start [this]                                               \n"
				+ "         (if (component/started? this)                             \n"
				+ "           (do (println \":server already started\") this)         \n"
				+ "           (do (println \":server started\") this)))               \n"
				+ "       (stop [this]                                                \n"
				+ "         (if (component/stopped? this)                             \n"
				+ "           (do (println \":server already stopped\") this)         \n"
				+ "           (do (println \":server stopped\") this)))               \n"
				+ "       (inject [this deps]                                         \n"
				+ "          (assoc this :components deps)))                          \n"
				+ "                                                                   \n"
				+ "  (deftype :database [user       :string                           \n"
				+ "                      password   :string                           \n"
				+ "                      components :map ]                            \n"
				+ "     component/Component                                           \n"
				+ "       (start [this]                                               \n"
				+ "         (if (component/started? this)                             \n"
				+ "           (do (println \":database already started\") this)       \n"
				+ "           (do (println \":database started\") this)))             \n"
				+ "       (stop [this]                                                \n"
				+ "         (if (component/stopped? this)                             \n"
				+ "           (do (println \":database already stopped\") this)       \n"
				+ "           (do (println \":database stopped\") this)))             \n"
				+ "       (inject [this deps]                                         \n"
				+ "          (assoc this :components deps)))                          \n"
				+ "                                                                   \n"
				+ "  (defn create-system []                                           \n"
				+ "    (-> (component/system-map                                      \n"
				+ "           \"test\"                                                \n"
				+ "           :server (server. 4600 {})                               \n"
				+ "           :store  (database. \"foo\" \"123\" {}))                 \n"
				+ "        (component/system-using {:server [:store]})))              \n"
				+ "                                                                   \n"
				+ "  (def system (create-system))                                     \n"
				+ "                                                                   \n"
				+ "  (with-out-str                                                    \n"
				+ "    (set! system (component/start system))                         \n"
				+ "    (set! system (component/start system))                         \n"
				+ "    (set! system (component/stop system))))                          "; 

		assertEquals(
			":database started\n" +
			":server started\n" +
			":database already started\n" +
			":server already started\n" +
			":server stopped\n" +
			":database stopped\n",
			venice.eval(script));
	}

}
