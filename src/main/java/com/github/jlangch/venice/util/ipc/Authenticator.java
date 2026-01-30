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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.util.io.IOStreamUtil;
import com.github.jlangch.venice.util.ipc.impl.Acl;
import com.github.jlangch.venice.util.ipc.impl.util.Json;
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

    public void clear() {
        clearCredentials();
        clearQueueAcl();
        clearTopicAcl();
    }


    // ------------------------------------------------------------------------
    // Credentials
    // ------------------------------------------------------------------------

    public int getCredentialsCount() {
        return authorizations.size();
    }

    public void addCredentials(
            final String principal,
            final String password
    ) {
        addCredentials(principal, password, false);
    }

    public void addCredentials(
            final String principal,
            final String password,
            final boolean adminRole
    ) {
        Objects.requireNonNull(principal);
        Objects.requireNonNull(password);

        if (principal.length() > MAX_LEN_USER) {
            throw new IpcException("A principal name is limited to " + MAX_LEN_USER + " characters");
        }

        if (password.length() > MAX_LEN_PWD) {
            throw new IpcException("A password is limited to " + MAX_LEN_PWD + " characters");
        }

        authorizations.put(principal, new Auth(principal, pwEncoder.encode(password), adminRole));
    }

    public void removeCredentials(final String principal) {
        Objects.requireNonNull(principal);

        authorizations.remove(principal);
    }

    public void clearCredentials() {
        authorizations.clear();
    }

    public boolean isAuthenticated(
            final String principal,
            final String password
    ) {
        if (!active) {
           return true;
        }

        if (principal == null || password == null) {
            return false;
        }

        final Auth auth = authorizations.get(principal);
        return auth != null && pwEncoder.verify(password, auth.pwHash);
    }

    public boolean isAdmin(final String principal) {
        if (!active || principal == null) {
           return false;
        }

        final Auth auth = authorizations.get(principal);
        return auth != null && auth.adminRole;
    }



    // ------------------------------------------------------------------------
    // ACL
    // ------------------------------------------------------------------------

    public void setQueueAcl(
            final String queueName,
            final AccessMode accessMode,
            final String principal
    ) {
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(accessMode);

        final Acl acl = new Acl(queueName, principal, accessMode);

        Map<String,Acl> acls = queueAcls.get(queueName);
        if (acls == null) {
            acls = new HashMap<>();
        }
        acls.put(acl.getPrincipal(), acl);
        queueAcls.put(queueName, acls);
    }

    public Map<String,Acl> getQueueAcls(
            final String queueName
    ) {
        Objects.requireNonNull(queueName);

        Map<String,Acl> acls = queueAcls.get(queueName);
        return acls == null ? new HashMap<>() : acls;
    }

    public void clearQueueAcl() {
    }


    public void setTopicAcl(
            final String topicName,
            final AccessMode accessMode,
            final String principal
    ) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(accessMode);

        final Acl acl = new Acl(topicName, principal, accessMode);

        Map<String,Acl> acls = topicAcls.get(topicName);
        if (acls == null) {
            acls = new HashMap<>();
        }
        acls.put(acl.getPrincipal(), acl);
        topicAcls.put(topicName, acls);
    }

    public Map<String,Acl> getTopicAcls(
            final String topicName
    ) {
        Objects.requireNonNull(topicName);

        Map<String,Acl> acls = topicAcls.get(topicName);
        return acls == null ? new HashMap<>() : acls;
    }

    public void clearTopicAcl() {
    }


    // ------------------------------------------------------------------------
    // Load/Save
    // ------------------------------------------------------------------------

    public void load(final InputStream is) {
        Objects.requireNonNull(is);

        try {
            clearCredentials();

            final byte[] data = IOStreamUtil.copyIStoByteArray(is);

            final String json = new String(data, StandardCharsets.UTF_8);

            final VncMap map = (VncMap)Json.readJson(json, false);

            final VncList auths = (VncList)map.get(new VncString("authorizations"));
            final VncList qacls = (VncList)map.get(new VncString("queue-acls"));
            final VncList tacls = (VncList)map.get(new VncString("topic-acls"));

            auths.forEach(a -> { Auth auth = toAuth((VncMap)a);
                                 authorizations.put(auth.principal, auth); });

            qacls.forEach(a -> { Acl acl = toAcl((VncMap)a);
                                 setQueueAcl(acl.getSubject(), acl.getMode(), acl.getPrincipal()); });

            tacls.forEach(a -> { Acl acl = toAcl((VncMap)a);
                                 setTopicAcl(acl.getSubject(), acl.getMode(), acl.getPrincipal()); });

            // automatically active after loading credentials
            activate(true);
        }
        catch(IOException ex) {
            throw new IpcException("Failed to load authenticator data", ex);
        }
    }

    public void save(final OutputStream os) {
        Objects.requireNonNull(os);

        final List<VncVal> auths = authorizations
                                    .values()
                                    .stream()
                                    .map(a->toVncMap(a))
                                    .collect(Collectors.toList());

        final List<VncVal> qacls = toAclList(queueAcls)
                                    .stream()
                                    .map(a->toVncMap(a))
                                    .collect(Collectors.toList());

        final List<VncVal> tacls = toAclList(topicAcls)
                                    .stream()
                                    .map(a->toVncMap(a))
                                    .collect(Collectors.toList());

        final VncHashMap data = VncHashMap.of(
                                    new VncString("authorizations"),
                                    VncList.ofColl(auths),
                                    new VncString("queue-acls"),
                                    VncList.ofColl(qacls),
                                    new VncString("topic-acls"),
                                    VncList.ofColl(tacls));

        final String json = Json.writeJson(data, true);

        try (OutputStreamWriter osr = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            osr.write(json);
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


    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    public static boolean isAdminRole(final String role) {
        return ADMIN_ROLE.equals(role);
    }

    private static VncMap toVncMap(final Auth auth) {
        return VncHashMap.of(
                new VncString("principal"),
                new VncString(auth.principal),
                new VncString("pw-hash"),
                new VncString(auth.pwHash),
                new VncString("admin"),
                VncBoolean.of(auth.adminRole));
    }

    private static VncMap toVncMap(final Acl acl) {
        return VncHashMap.of(
                new VncString("subject"),
                new VncString(acl.getSubject()),
                new VncString("principal"),
                new VncString(acl.getPrincipal()),
                new VncString("access"),
                new VncString(acl.getMode().name()));
    }

    private static Auth toAuth(final VncMap auth) {
        return new Auth(
                ((VncString)auth.get(new VncString("principal"))).getValue(),
                ((VncString)auth.get(new VncString("pw-hash"))).getValue(),
                ((VncBoolean)auth.get(new VncString("admin"))).getValue());
    }

    private static Acl toAcl(final VncMap auth) {
        return new Acl(
                ((VncString)auth.get(new VncString("subject"))).getValue(),
                ((VncString)auth.get(new VncString("principal"))).getValue(),
                AccessMode.valueOf(((VncString)auth.get(new VncString("access"))).getValue()));
    }

    private static List<Acl> toAclList(final Map<String, Map<String,Acl>> data) {
        final List<Acl> list = new ArrayList<>();
        for(Map<String,Acl> m : data.values()) {
            list.addAll(m.values());
        }
        return list;
    }

    private static class Auth {
        public Auth(final String principal, final String pwHash, final boolean adminRole) {
            Objects.requireNonNull(principal);
            Objects.requireNonNull(pwHash);
            this.principal = principal;
            this.pwHash = pwHash;
            this.adminRole = adminRole;
        }

        public final String principal;
        public final String pwHash;
        public final boolean adminRole;
    }



    public final static String ADMIN_ROLE = "admin";

    private final static int MAX_LEN_USER = 100;
    private final static int MAX_LEN_PWD = 50;

    private volatile boolean active;

    private final PBKDF2PasswordEncoder pwEncoder = new PBKDF2PasswordEncoder();
    private final ConcurrentHashMap<String, Auth> authorizations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Acl>> queueAcls = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, Acl>> topicAcls = new ConcurrentHashMap<>();
}
