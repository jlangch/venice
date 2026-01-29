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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.util.password.PBKDF2PasswordEncoder;


/**
 * The authenticator stores passwords as salted PBKDF2 hashes! It does not keep
 * the clear text passwords.
 */
public class Authenticator {

    public Authenticator(final boolean activate) {
        this.active = activate;
    }

    public void activate(final boolean activate) {
        this.active = activate;
    }

    public boolean isActive() {
        return active;
    }

    public int size() {
        return authorizations.size();
    }

    public void addCredentials(
            final String userName,
            final String password
    ) {
        addCredentials(userName, password, false);
    }

    public void addCredentials(
            final String userName,
            final String password,
            final boolean adminRole
    ) {
        Objects.requireNonNull(userName);
        Objects.requireNonNull(password);

        if (userName.length() > MAX_LEN) {
            throw new IpcException("A user name is limited to " + MAX_LEN + " characters");
        }

        if (password.length() > MAX_LEN) {
            throw new IpcException("A password is limited to " + MAX_LEN + " characters");
        }

        authorizations.put(userName, new Auth(pwEncoder.encode(password), adminRole));
    }

    public void removeCredentials(final String userName) {
        Objects.requireNonNull(userName);

        authorizations.remove(userName);
    }

    public void clearCredentials() {
        authorizations.clear();
    }

    public boolean isAuthenticated(
            final String userName,
            final String password
    ) {
        if (!active) {
           return true;
        }

        if (userName == null || password == null) {
            return false;
        }

        final Auth auth = authorizations.get(userName);
        return auth != null && pwEncoder.verify(password, auth.pwHash);
    }


    public boolean isAdmin(final String userName) {
        if (!active || userName == null) {
           return false;
        }

        final Auth auth = authorizations.get(userName);
        return auth != null && auth.adminRole;
    }

    public void load(final InputStream is) {
        Objects.requireNonNull(is);

        try (InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"))) {
            clearCredentials();

            final Properties p = new Properties();
            p.load(isr);
            p.forEach((k,v)-> authorizations.put((String)k, decodeAuth((String)v)));

            // automatically active after loading credentials
            activate(true);
        }
        catch(IOException ex) {
            throw new IpcException("Failed to load authenticator data", ex);
        }
    }

    public void save(final OutputStream os) {
        Objects.requireNonNull(os);

        try (OutputStreamWriter osr = new OutputStreamWriter(os, Charset.forName("UTF-8"))) {
            final Properties p = new Properties();
            authorizations.forEach((k,v) -> p.setProperty(k, encodeAuth(v)));
            p.store(osr, "IPC user credentials");
            osr.flush();
        }
        catch(IOException ex) {
            throw new IpcException("Failed to save authenticator data", ex);
        }
    }

    public void load(final File f) {
        Objects.requireNonNull(f);

        try (InputStream is = new FileInputStream(f)) {
            load(is);
        }
        catch(IOException ex) {
            throw new IpcException("Failed to load authenticator data", ex);
        }
    }

    public void save(final File f) {
        Objects.requireNonNull(f);

        try (OutputStream is = new FileOutputStream(f)) {
            save(is);
        }
        catch(IOException ex) {
            throw new IpcException("Failed to save authenticator data", ex);
        }
    }

    public static boolean isAdminRole(final String role) {
        return ADMIN_ROLE.equals(role);
    }

    private static String encodeAuth(final Auth auth) {
        return String.format(
                "%s::%s",
                auth.adminRole ? ADMIN_ROLE : "-",
                auth.pwHash);
    }

    private static Auth decodeAuth(final String auth) {
        final int pos = auth.indexOf("::");
        if (pos < 0) {
            return new Auth(auth, false);
        }
        else {
            final String role = auth.substring(0, pos);
            final String pwHash = auth.substring(pos + "::".length());
            return new Auth(pwHash, isAdminRole(role));
        }
    }

    private static class Auth {
        public Auth(final String pwHash, final boolean adminRole) {
            Objects.requireNonNull(pwHash);
            this.pwHash = pwHash;
            this.adminRole = adminRole;
        }

        public final String pwHash;
        public final boolean adminRole;
    }


    public final static String ADMIN_ROLE = "admin";

    private final static int MAX_LEN = 100;

    private volatile boolean active;

    private final PBKDF2PasswordEncoder pwEncoder = new PBKDF2PasswordEncoder();
    private final ConcurrentHashMap<String, Auth> authorizations = new ConcurrentHashMap<>();
}
