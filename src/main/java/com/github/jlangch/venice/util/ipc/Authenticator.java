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
        Objects.requireNonNull(userName);
        Objects.requireNonNull(password);

        authorizations.put(userName, pwEncoder.encode(password));
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

        final String pwHash = authorizations.get(userName);
        return pwHash != null && pwEncoder.verify(password, pwHash);
    }

    public void load(final InputStream is) {
        Objects.requireNonNull(is);

        try (InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"))) {
            clearCredentials();

            final Properties p = new Properties();
            p.load(isr);
            p.forEach((k,v)-> authorizations.put((String)k, (String)v));

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
            authorizations.forEach((k,v) -> p.setProperty(k, v));
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



    private volatile boolean active;

    private final PBKDF2PasswordEncoder pwEncoder = new PBKDF2PasswordEncoder();
    private final ConcurrentHashMap<String, String> authorizations = new ConcurrentHashMap<>();
}
