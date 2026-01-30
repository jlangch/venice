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
    public void test_add_credentials() {
        final Authenticator a = new Authenticator(false);

        assertEquals(0, a.getCredentialsCount());

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);

        assertEquals(2, a.getCredentialsCount());

        a.removeCredentials("u1");

        assertEquals(1, a.getCredentialsCount());

        a.removeCredentials("u2");

        assertEquals(0, a.getCredentialsCount());

        a.addCredentials("u1", "123");
        a.addCredentials("u2", "456");

        assertEquals(2, a.getCredentialsCount());

        a.clearCredentials();

        assertEquals(0, a.getCredentialsCount());
    }

    @Test
    public void test_add_remove_clear_credentials() {
        final Authenticator a = new Authenticator(true);

        assertEquals(0, a.getCredentialsCount());

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);

        assertEquals(2, a.getCredentialsCount());

        assertTrue(a.isAuthenticated("u1", "123"));
        assertTrue(a.isAuthenticated("u2", "456"));

        a.removeCredentials("u2");

        assertEquals(1, a.getCredentialsCount());

        assertTrue(a.isAuthenticated("u1", "123"));
        assertFalse(a.isAuthenticated("u2", "456"));

        a.clearCredentials();

        assertEquals(0, a.getCredentialsCount());

        assertFalse(a.isAuthenticated("u1", "123"));
        assertFalse(a.isAuthenticated("u2", "456"));
    }

    @Test
    public void test_authenticated_on() {
        final Authenticator a = new Authenticator(true);

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);

        assertTrue(a.isActive());

        assertTrue(a.isAuthenticated("u1", "123"));
        assertTrue(a.isAuthenticated("u2", "456"));

        assertTrue(a.isAdmin("u1"));
        assertFalse(a.isAdmin("u2"));

        assertFalse(a.isAuthenticated("u1", "124"));
        assertFalse(a.isAuthenticated("x1", "123"));

        assertFalse(a.isAuthenticated("u1", null));
        assertFalse(a.isAuthenticated(null, "124"));
        assertFalse(a.isAuthenticated(null, null));
    }

    @Test
    public void test_authenticated_on_utf8() {
        final Authenticator a = new Authenticator(true);

        a.addCredentials("u-α", "123-α", true);
        a.addCredentials("u-β", "456-β", false);

        assertTrue(a.isActive());

        assertTrue(a.isAuthenticated("u-α", "123-α"));
        assertTrue(a.isAuthenticated("u-β", "456-β"));

        assertTrue(a.isAdmin("u-α"));
        assertFalse(a.isAdmin("u-β"));

        assertFalse(a.isAuthenticated("u-α", "123-γ"));
        assertFalse(a.isAuthenticated("x1", "123-γ"));

        assertFalse(a.isAuthenticated("u1", null));
        assertFalse(a.isAuthenticated(null, "124"));
        assertFalse(a.isAuthenticated(null, null));
    }

    @Test
    public void test_authenticated_off() {
        final Authenticator a = new Authenticator(false);

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);

        assertTrue(a.isAuthenticated("u1", "123"));
        assertTrue(a.isAuthenticated("u2", "456"));

        assertFalse(a.isActive());

        assertFalse(a.isAdmin("u1"));
        assertFalse(a.isAdmin("u2"));

        // always ok!
        assertTrue(a.isAuthenticated("u1", "124"));
        assertTrue(a.isAuthenticated("x1", "123"));

        // always ok!
        assertTrue(a.isAuthenticated("u1", null));
        assertTrue(a.isAuthenticated(null, "124"));
        assertTrue(a.isAuthenticated(null, null));
    }

    @Test
    public void test_authenticated_load_save_1() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);

        assertFalse(a.isAdmin("u1"));  // Authenticator turned off
        assertFalse(a.isAdmin("u2"));  // Authenticator turned off

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertTrue(b.isActive());  // automatically active after loading credentials
        assertEquals(2, b.getCredentialsCount());

        assertTrue(b.isAuthenticated("u1", "123"));
        assertTrue(b.isAuthenticated("u2", "456"));

        assertTrue(b.isAdmin("u1"));
        assertFalse(b.isAdmin("u2"));
   }

    @Test
    public void test_authenticated_load_save_2() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u1", "123", true);
        a.addCredentials("u2", "456", false);
        a.addCredentials("u3", "x------------------------x", false);

        assertFalse(a.isAdmin("u1"));  // Authenticator turned off
        assertFalse(a.isAdmin("u2"));  // Authenticator turned off

        a.save(file);

        final Authenticator b = new Authenticator(false);
        b.load(file);

        assertTrue(b.isActive());  // automatically active after loading credentials
        assertEquals(3, b.getCredentialsCount());

        assertTrue(b.isAuthenticated("u1", "123"));
        assertTrue(b.isAuthenticated("u2", "456"));
        assertTrue(b.isAuthenticated("u3", "x------------------------x"));

        assertTrue(b.isAdmin("u1"));
        assertFalse(b.isAdmin("u2"));
        assertFalse(b.isAdmin("u3"));
    }

    @Test
    public void test_authenticated_load_save_1_utf8() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u-α", "123-α", true);
        a.addCredentials("u-β", "456-β", false);

        a.save(new FileOutputStream(file));

        final Authenticator b = new Authenticator(false);
        b.load(new FileInputStream(file));

        assertTrue(b.isActive());  // automatically active after loading credentials
        assertEquals(2, b.getCredentialsCount());

        assertTrue(b.isAuthenticated("u-α", "123-α"));
        assertTrue(b.isAuthenticated("u-β", "456-β"));

        assertTrue(b.isAdmin("u-α"));
        assertFalse(b.isAdmin("u-β"));
   }

    @Test
    public void test_authenticated_load_save_2_utf8() throws Exception {
        final File file = Files.createTempFile("test", ".cred").normalize().toFile();
        file.deleteOnExit();

        final Authenticator a = new Authenticator(false);

        a.addCredentials("u-α", "123-α", true);
        a.addCredentials("u-β", "456-β", false);

        a.save(file);

        final Authenticator b = new Authenticator(false);
        b.load(file);

        assertTrue(b.isActive());  // automatically active after loading credentials
        assertEquals(2, b.getCredentialsCount());

        assertTrue(b.isAuthenticated("u-α", "123-α"));
        assertTrue(b.isAuthenticated("u-β", "456-β"));

        assertTrue(b.isAdmin("u-α"));
        assertFalse(b.isAdmin("u-β"));
    }

}
