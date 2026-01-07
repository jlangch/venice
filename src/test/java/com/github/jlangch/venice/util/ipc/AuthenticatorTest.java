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
package com.github.jlangch.venice.util.ipc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;


public class AuthenticatorTest {

    @Test
    public void test_active() {
        assertFalse(new Authenticator(false).isActive());
        assertTrue(new Authenticator(true).isActive());

        final Authenticator a = new Authenticator(false);
        assertFalse(a.isActive());

        a.activate(true);
        assertTrue(a.isActive());

        a.activate(false);
        assertFalse(a.isActive());
   }

    @Test
    public void test_credentials() {
        final Authenticator a = new Authenticator(false);

        assertEquals(0, a.size());

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        assertEquals(2, a.size());

        a.removeCredentials("u1");

        assertEquals(1, a.size());

        a.removeCredentials("u2");

        assertEquals(0, a.size());

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        assertEquals(2, a.size());

        a.clearCredentials();

        assertEquals(0, a.size());
    }

    @Test
    public void test_authenticated_on() {
        final Authenticator a = new Authenticator(true);

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        assertTrue(a.isAuthenticated("u1", "123"));
        assertTrue(a.isAuthenticated("u2", "456"));

        assertFalse(a.isAuthenticated("u1", "124"));
        assertFalse(a.isAuthenticated("x1", "123"));

        assertFalse(a.isAuthenticated("u1", null));
        assertFalse(a.isAuthenticated(null, "124"));
        assertFalse(a.isAuthenticated(null, null));
    }

    @Test
    public void test_authenticated_off() {
        final Authenticator a = new Authenticator(false);

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        assertTrue(a.isAuthenticated("u1", "123"));
        assertTrue(a.isAuthenticated("u2", "456"));

        // always ok!
        assertTrue(a.isAuthenticated("u1", "124"));
        assertTrue(a.isAuthenticated("x1", "123"));

        // always ok!
        assertTrue(a.isAuthenticated("u1", null));
        assertTrue(a.isAuthenticated(null, "124"));
        assertTrue(a.isAuthenticated(null, null));
    }

    @Test
    public void test_authenticated_load_save() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertEquals(2, b.size());

        assertTrue(b.isAuthenticated("u1", "123"));
        assertTrue(b.isAuthenticated("u2", "456"));
     }

    @Test
    public void test_authenticated_load_save_utf8() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u-α", "123-α");
        a.addCredentials("u-β", "456-β");

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertEquals(2, b.size());

        assertTrue(b.isAuthenticated("u-α", "123-α"));
        assertTrue(b.isAuthenticated("u-β", "456-β"));
     }

}
