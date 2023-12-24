/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class ConfigModuleTest {

    @Test
    public void test_thread_ks() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" nil)))                \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" \"java\")))           \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" \"java.\")))          \n" +
                "                                                          \n" +
                "   (assert (= '(:home)                                    \n" +
                "              (config/->ks \"java\" \"java.home\")))      \n" +
                "                                                          \n" +
                "   (assert (= '(:home :vm)                                \n" +
                "              (config/->ks \"java\" \"java.home.vm\"))))    ";

        venice.eval(script);
    }

    @Test
    public void test_thread_ks_empty_prefix() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"\" nil)))                    \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks \"\" \"java\")))               \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks \"\" \"java.\")))              \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home)                              \n" +
                "              (config/->ks \"\" \"java.home\")))          \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home :vm)                          \n" +
                "              (config/->ks \"\" \"java.home.vm\"))))        ";

        venice.eval(script);
    }

    @Test
    public void test_thread_ks_nil_prefix() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks nil nil)))                     \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks nil \"java\")))                \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks nil \"java.\")))               \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home)                              \n" +
                "              (config/->ks nil \"java.home\")))           \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home :vm)                          \n" +
                "              (config/->ks nil \"java.home.vm\"))))        ";

        venice.eval(script);
    }

    @Test
    public void test_thread_ks_unmatching_prefix() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"kava\" \"java\")))           \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"kava\" \"java.\")))          \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"kava\" \"java.home\")))      \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"kava\" \"java.home.vm\"))))    ";
        venice.eval(script);
    }

    @Test
    public void test_thread_ks_prefix_with_dot() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java.\" nil)))               \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java.\" \"java\")))          \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java.\" \"java.\")))         \n" +
                "                                                          \n" +
                "   (assert (= '(:home)                                    \n" +
                "              (config/->ks \"java.\" \"java.home\")))     \n" +
                "                                                          \n" +
                "   (assert (= '(:home :vm)                                \n" +
                "              (config/->ks \"java.\" \"java.home.vm\"))))    ";

        venice.eval(script);
    }

    @Test
    public void test_thread_ks_lowercase_underscore_mapping() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" nil)))                \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" \"java\")))           \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks \"java\" \"java_\")))          \n" +
                "                                                          \n" +
                "   (assert (= '(:home)                                    \n" +
                "              (config/->ks \"java\" \"java_home\")))      \n" +
                "                                                          \n" +
                "   (assert (= '(:home :vm)                                \n" +
                "              (config/->ks \"jaVa\" \"Java_Home_Vm\"))))    ";

        venice.eval(script);
    }

    @Test
    public void test_thread_ks_special_cases() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks nil \"_\")))                   \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/->ks nil \"__\")))                  \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks nil \"_java\")))               \n" +
                "                                                          \n" +
                "   (assert (= '(:java)                                    \n" +
                "              (config/->ks nil \"__java\")))              \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home)                              \n" +
                "              (config/->ks nil \"_java_home_\")))         \n" +
                "                                                          \n" +
                "   (assert (= '(:java :home)                              \n" +
                "              (config/->ks nil \"__java__home__\"))))       ";

        venice.eval(script);
    }

    @Test
    public void test_env_var() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/env-var \"M_SERVER_PORT\"           \n" +
                "                              [:http :port])))            \n" +
                "                                                          \n" +
                "   (assert (= {:http {:port \"8080\"}}                    \n" +
                "              (config/env-var \"M_SERVER_PORT\"           \n" +
                "                                   [:http :port]          \n" +
                "                                   \"8080\"))))             ";

        venice.eval(script);
    }

    @Test
    public void test_property_var() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "   (load-module :config)                                  \n" +
                "                                                          \n" +
                "   (assert (= nil                                         \n" +
                "              (config/property-var \"M_SERVER_PORT\"      \n" +
                "                                   [:http :port])))       \n" +
                "                                                          \n" +
                "   (assert (= {:http {:port \"8080\"}}                    \n" +
                "              (config/property-var \"M_SERVER_PORT\"      \n" +
                "                                   [:http :port]          \n" +
                "                                   \"8080\"))))             ";

        venice.eval(script);
    }

    @Test
    public void test_build_empty() {
        final Venice venice = new Venice();

        final String script =
                "(do                        \n" +
                "  (load-module :config)    \n" +
                "  (pr-str (config/build)))   ";

        assertEquals("{}", venice.eval(script));
    }

    @Test
    public void test_build_env_var() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (load-module :config)                   \n" +
                "                                          \n" +
                "  (defn config []                         \n" +
                "    (config/build                         \n" +
                "      (config/env-var \"M_SERVER_PORT\"   \n" +
                "                      [:http :port]       \n" +
                "                      \"8000\")))         \n" +
                "                                          \n" +
                "  (-> (config) :http :port))                ";

        assertEquals("8000", venice.eval(script));
    }

    @Test
    public void test_build_env_var_override() {
        final Venice venice = new Venice();

        final String script =
                "(do                                       \n" +
                "  (load-module :config)                   \n" +
                "                                          \n" +
                "  (defn config []                         \n" +
                "    (config/build                         \n" +
                "      (config/env-var \"M_SERVER_PORT\"   \n" +
                "                      [:http :port]       \n" +
                "                      \"4000\")           \n" +
                "      (config/env-var \"M_SERVER_PORT\"   \n" +
                "                      [:http :port]       \n" +
                "                      \"8000\")))         \n" +
                "                                          \n" +
                "  (-> (config) :http :port))                ";

        assertEquals("8000", venice.eval(script));
    }

    @Test
    public void test_build_json() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       \n" +
                "  (load-module :config)                                   \n" +
                "                                                          \n" +
                "  (def app-cfg \"\"\"{\"app\": {\"pwd\": \"123\"}}\"\"\") \n" +
                "                                                          \n" +
                "  (defn config []                                         \n" +
                "    (config/build                                         \n" +
                "      (json/read-str app-cfg :key-fn keyword)             \n" +
                "      (config/env-var \"M_SERVER_PORT\"                   \n" +
                "                      [:http :port]                       \n" +
                "                      \"8000\")))                         \n" +
                "                                                          \n" +
                "  (pr-str [ (-> (config) :app :pwd)                       \n" +
                "            (-> (config) :http :port)]))                    ";

        assertEquals("[\"123\" \"8000\"]", venice.eval(script));
    }

    @Test
    public void test_build_json_file() {
        final Venice venice = new Venice();

        final String script =
                "(do                                             \n" +
                "  (load-module :config)                         \n" +
                "                                                \n" +
                "  (defn config []                               \n" +
                "    (config/build                               \n" +
                "      (config/file file-name :key-fn keyword)   \n" +
                "      (config/env-var \"M_SERVER_PORT\"         \n" +
                "                      [:http :port]             \n" +
                "                      \"8000\")))               \n" +
                "                                                \n" +
                "  (pr-str [ (-> (config) :app :pwd)             \n" +
                "            (-> (config) :http :port)]))          ";

        try {
            final File file = Files.createTempFile("from__", ".json").normalize().toFile();
            file.deleteOnExit();
            final String fileName = file.getAbsolutePath();

            // write
            venice.eval(
                    "(io/spit (io/file file-name) \n" +
                    "           \"\"\"{\"app\": {\"pwd\": \"123\"}}\"\"\")",
                    Parameters.of("file-name", fileName));

            // test
            assertEquals(
                    "[\"123\" \"8000\"]",
                    venice.eval(
                            script,
                            Parameters.of("file-name", fileName)));
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
