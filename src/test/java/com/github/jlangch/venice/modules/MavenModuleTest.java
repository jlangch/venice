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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class MavenModuleTest {

    @Test
    public void test_uri_jar() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                         " +
                "   (load-module :maven)                                     " +
                "                                                            " +
                "   (maven/artifact-uri \"org.knowm.xchart:xchart:3.6.1\"    " +
                "                       \".jar\")                            " +
                ") ";

        assertEquals(
                "https://repo1.maven.org/maven2/org/knowm/xchart/xchart/3.6.1/xchart-3.6.1.jar",
                venice.eval(script));
    }

    @Test
    public void test_uri_sources() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                        " +
                "   (load-module :maven)                                    " +
                "                                                           " +
                "   (maven/artifact-uri \"org.knowm.xchart:xchart:3.6.1\"   " +
                "                       \"-sources.jar\")                   " +
                ") ";

        assertEquals(
                "https://repo1.maven.org/maven2/org/knowm/xchart/xchart/3.6.1/xchart-3.6.1-sources.jar",
                venice.eval(script));
    }

    @Test
    public void test_uri_pom() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                       " +
                "   (load-module :maven)                                   " +
                "                                                          " +
                "   (maven/artifact-uri \"org.knowm.xchart:xchart:3.6.1\"  " +
                "                       \".pom\")                          " +
                ") ";

        assertEquals(
                "https://repo1.maven.org/maven2/org/knowm/xchart/xchart/3.6.1/xchart-3.6.1.pom",
                venice.eval(script));
    }

    @Test
    public void test_get_jar() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      " +
                "   (load-module :maven)                                  " +
                "                                                         " +
                "   (if (io/internet-avail?)                              " +
                "     (maven/get \"org.knowm.xchart:xchart:3.6.1\" :jar)  " +
                "     :no-internet)                                       " +
                ") ";

        final Object result = venice.eval(script);

        if (result instanceof String) {
            assertEquals("no-internet", result);
        }
        else if (result instanceof ByteBuffer) {
            assertEquals(320835, ((ByteBuffer)result).remaining());
        }
        else {
            fail("got " + result.getClass().toGenericString());
        }
    }

    @Test
    public void test_get_sources() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                          " +
                "   (load-module :maven)                                      " +
                "                                                             " +
                "   (if (io/internet-avail?)                                  " +
                "     (maven/get \"org.knowm.xchart:xchart:3.6.1\" :sources)  " +
                "     :no-internet)                                           " +
                ") ";

        final Object result = venice.eval(script);

        if (result instanceof String) {
            assertEquals("no-internet", result);
        }
        else if (result instanceof ByteBuffer) {
            assertEquals(174665, ((ByteBuffer)result).remaining());
        }
        else {
            fail("got " + result.getClass().toGenericString());
        }
    }

    @Test
    public void test_get_pom() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                      " +
                "   (load-module :maven)                                  " +
                "                                                         " +
                "   (if (io/internet-avail?)                              " +
                "     (maven/get \"org.knowm.xchart:xchart:3.6.1\" :pom)  " +
                "     :no-internet)                                       " +
                ") ";

        final Object result = venice.eval(script);

        if (result instanceof String) {
            assertEquals("no-internet", result);
        }
        else if (result instanceof ByteBuffer) {
            assertEquals(799, ((ByteBuffer)result).remaining());
        }
        else {
            fail("got " + result.getClass().toGenericString());
        }
    }

    @Test
    public void test_download_pom() throws IOException {
        final Venice venice = new Venice();

        final File tmp = Files.createTempDirectory("maven").toFile();

        final String script =
                "(do                                                        " +
                "   (load-module :maven)                                    " +
                "                                                           " +
                "   (if (io/internet-avail?)                                " +
                "     (do                                                   " +
                "       (maven/download \"org.knowm.xchart:xchart:3.6.1\"   " +
                "                       :pom true                           " +
                "                       :dir dir)                           " +
                "       :downloaded)                                        " +
                "     :no-internet)                                         " +
                ") ";

        final String result = (String)venice.eval(script, Parameters.of("dir", tmp));

        if (result.equals("no-internet")) {
            assertTrue(true);
        }
        else if (result.equals("downloaded")) {
            final File pom = new File(tmp, "xchart-3.6.1.pom");
            assertTrue(pom.isFile());
            pom.delete();
        }
        else {
            fail("got " + result);
        }
    }
}

