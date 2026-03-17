/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.impl.util.io.FileUtil;


public class RSAModuleTest {

    @Test
    public void test_generate_key_pair() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                         \n" +
                "    (instance-of? :java.security.KeyPair key-pair)))              ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_key_pair() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [[pub priv] (rsa/keys (rsa/generate-key-pair))             \n" +
                "        key-pair   (rsa/key-pair pub priv)]                       \n" +
                "    (instance-of? :java.security.KeyPair key-pair)))              ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_keys() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [[pub priv] (rsa/keys (rsa/generate-key-pair))]            \n" +
                "    (and (instance-of? :java.security.PrivateKey priv)            \n" +
                "         (instance-of? :java.security.PublicKey pub))))           ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_generate_public_key() {
        final Venice venice = new Venice();

        final String script =
                "(do                                          \n" +
                "  (load-module :rsa)                         \n" +
                "  (let [key-pair (rsa/generate-key-pair)]    \n" +
                "    (rsa/public-key key-pair)))              ";

        assertTrue(venice.eval(script) instanceof PublicKey);
    }

    @Test
    public void test_generate_private_key() {
        final Venice venice = new Venice();

        final String script =
                "(do                                          \n" +
                "  (load-module :rsa)                         \n" +
                "  (let [key-pair (rsa/generate-key-pair)]    \n" +
                "    (rsa/private-key key-pair)))             ";

        assertTrue(venice.eval(script) instanceof PrivateKey);
    }

    @Test
    public void test_save() throws Exception {
        final File dir = Files.createTempDirectory("test").toFile();

        try {
            final Venice venice = new Venice();

            final String script =
                    "(do                                              \n" +
                    "  (load-module :rsa)                             \n" +
                    "  (let [key-pair (rsa/generate-key-pair)]        \n" +
                    "    (rsa/save-key-pair key-pair dir \"demo\")))  ";

            venice.eval(script, Parameters.of("dir", dir));

            assertTrue(new File(dir, "demo-public.pem").isFile());
            assertTrue(new File(dir, "demo-private.pem").isFile());

        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    public void test_load_key_pair() throws Exception {
        final File dir = Files.createTempDirectory("test").toFile();

        try {
            final Venice venice = new Venice();

            final String script =
                    "(do                                                     \n" +
                    "  (load-module :rsa)                                    \n" +
                    "  (let [key-pair (rsa/generate-key-pair)]               \n" +
                    "    (rsa/save-key-pair key-pair dir \"demo\")           \n" +
                    "    (rsa/load-key-pair dir \"demo\")))                  ";

            final Object key = venice.eval(script, Parameters.of("dir", dir));

            assertTrue(key instanceof KeyPair);
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    public void test_load_public_key() throws Exception {
        final File dir = Files.createTempDirectory("test").toFile();

        try {
            final Venice venice = new Venice();

            final String script =
                    "(do                                                     \n" +
                    "  (load-module :rsa)                                    \n" +
                    "  (let [key-pair (rsa/generate-key-pair)]               \n" +
                    "    (rsa/save-key-pair key-pair dir \"demo\")           \n" +
                    "    (rsa/load-key (io/file dir \"demo-public.pem\"))))  ";

            final Object key = venice.eval(script, Parameters.of("dir", dir));

            assertTrue(key instanceof PublicKey);
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    public void test_load_private_key() throws Exception {
        final File dir = Files.createTempDirectory("test").toFile();

        try {
            final Venice venice = new Venice();

            final String script =
                    "(do                                                     \n" +
                    "  (load-module :rsa)                                    \n" +
                    "  (let [key-pair (rsa/generate-key-pair)]               \n" +
                    "    (rsa/save-key-pair key-pair dir \"demo\")           \n" +
                    "    (rsa/load-key (io/file dir \"demo-private.pem\"))))  ";

            final Object key = venice.eval(script, Parameters.of("dir", dir));

            assertTrue(key instanceof PrivateKey);
        }
        catch(Exception ex) {
            throw ex;
        }
        finally {
            FileUtil.rmdir(dir);
        }

        assertFalse(dir.exists());
    }

    @Test
    public void test_encrypt_decrypt() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                               \n" +
                "  (load-module :rsa)                                              \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                         \n" +
                "    (-> (rsa/encrypt \"Hello World\" (rsa/public-key key-pair))   \n" +
                "        (rsa/decrypt (rsa/private-key key-pair)))))               ";

        assertEquals("Hello World", venice.eval(script));
    }

    @Test
    public void test_sign_verify() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :rsa)                                               \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                          \n" +
                "    (-> (rsa/sign \"Hello World\" (rsa/private-key key-pair))      \n" +
                "        (rsa/verify \"Hello World\" (rsa/public-key key-pair)))))  ";

        assertTrue((Boolean)venice.eval(script));
    }

    @Test
    public void test_sign_verify_tampered() {
        final Venice venice = new Venice();

        final String script =
                "(do                                                                \n" +
                "  (load-module :rsa)                                               \n" +
                "  (let [key-pair (rsa/generate-key-pair)]                          \n" +
                "    (-> (rsa/sign \"Hello World-\" (rsa/private-key key-pair))     \n" +
                "        (rsa/verify \"Hello World\" (rsa/public-key key-pair)))))  ";

        assertFalse((Boolean)venice.eval(script));
    }

}
