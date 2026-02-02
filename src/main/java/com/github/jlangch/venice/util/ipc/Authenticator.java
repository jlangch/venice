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
import java.util.concurrent.atomic.AtomicReference;
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
        removeAllQueueAcls();
        removeAllTopicAcls();
        removeAllFunctionAcls();
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

    public void setQueueDefaultAcl(final AccessMode mode) {
        Objects.requireNonNull(mode);
        queueDefault.set(new Acl("*", "*", mode));
    }

    public Acl getQueueDefaultAcl() {
        return queueDefault.get();
    }

    public void setTopicDefaultAcl(final AccessMode mode) {
        Objects.requireNonNull(mode);
        topicDefault.set(new Acl("*", "*", mode));
    }

    public Acl getTopicDefaultAcl() {
        return topicDefault.get();
    }

    public void setFunctionDefaultAcl(final AccessMode mode) {
        Objects.requireNonNull(mode);
        functionDefault.set(new Acl("*", "*", mode));
    }

    public Acl getFunctionDefaultAcl() {
        return functionDefault.get();
    }


    public void setQueueAcl(
            final String queueName,
            final AccessMode accessMode,
            final String principal
    ) {
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(accessMode);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = queueAcls.get(queueName);
        if (acls == null) {
            acls = new HashMap<>();
        }

        final Acl acl = new Acl(queueName, principal, accessMode);
        acls.put(acl.getPrincipal(), acl);
        queueAcls.put(queueName, acls);
    }

    public void removeQueueAcl(
            final String queueName,
            final String principal
    ) {
        Objects.requireNonNull(queueName);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = queueAcls.get(queueName);
        if (acls != null) {
            acls.remove(principal);
        }
    }

    public void removeQueueAcl(
            final String queueName
    ) {
        Objects.requireNonNull(queueName);

        queueAcls.remove(queueName);
    }

    public void removeAllQueueAcls() {
        queueAcls.clear();
    }

    public Map<String,Acl> getQueueAclsMappedByPrincipal(final String queueName) {
        Objects.requireNonNull(queueName);

        final Map<String,Acl> acls = queueAcls.get(queueName);
        return acls == null ? new HashMap<>() : acls;
    }


    public void setTopicAcl(
            final String topicName,
            final AccessMode accessMode,
            final String principal
    ) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(accessMode);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = topicAcls.get(topicName);
        if (acls == null) {
            acls = new HashMap<>();
        }

        final Acl acl = new Acl(topicName, principal, accessMode);
        acls.put(acl.getPrincipal(), acl);
        topicAcls.put(topicName, acls);
    }

    public void removeTopicAcl(
            final String topicName,
            final String principal
    ) {
        Objects.requireNonNull(topicName);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = topicAcls.get(topicName);
        if (acls != null) {
            acls.remove(principal);
        }
    }

    public void removeTopicAcl(
            final String topicName
    ) {
        Objects.requireNonNull(topicName);

        topicAcls.remove(topicName);
    }

    public Map<String,Acl> getTopicAclsMappedByPrincipal(final String topicName) {
        Objects.requireNonNull(topicName);

        final Map<String,Acl> acls = topicAcls.get(topicName);
        return acls == null ? new HashMap<>() : acls;
    }

    public void removeAllTopicAcls() {
        topicAcls.clear();
    }


    public void setFunctionAcl(
            final String functionName,
            final AccessMode accessMode,
            final String principal
    ) {
        Objects.requireNonNull(functionName);
        Objects.requireNonNull(accessMode);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = functionAcls.get(functionName);
        if (acls == null) {
            acls = new HashMap<>();
        }

        final Acl acl = new Acl(functionName, principal, accessMode);
        acls.put(acl.getPrincipal(), acl);
        functionAcls.put(functionName, acls);
    }

    public void removeFunctionAcl(
            final String functionName,
            final String principal
    ) {
        Objects.requireNonNull(functionName);
        Objects.requireNonNull(principal);

        Map<String,Acl> acls = functionAcls.get(functionName);
        if (acls != null) {
            acls.remove(principal);
        }
    }

    public void removeFunctionAcl(
            final String functionName
    ) {
        Objects.requireNonNull(functionName);

        functionAcls.remove(functionName);
    }

    public Map<String,Acl> getFunctionAclsMappedByPrincipal(final String functionName) {
        Objects.requireNonNull(functionName);

        final Map<String,Acl> acls = functionAcls.get(functionName);
        return acls == null ? new HashMap<>() : acls;
    }

    public void removeAllFunctionAcls() {
        functionAcls.clear();
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
            final VncList facls = (VncList)map.get(new VncString("function-acls"));

            final VncMap qdacl = (VncMap)map.get(new VncString("queue-default-acl"));
            final VncMap tdacl = (VncMap)map.get(new VncString("topic-default-acl"));
            final VncMap fdacl = (VncMap)map.get(new VncString("function-default-acl"));

            queueDefault.set(toAcl(qdacl));
            topicDefault.set(toAcl(tdacl));
            functionDefault.set(toAcl(fdacl));

            auths.forEach(a -> { Auth auth = toAuth((VncMap)a);
                                 authorizations.put(auth.principal, auth); });

            qacls.forEach(a -> { Acl acl = toAcl((VncMap)a);
                                 setQueueAcl(acl.getSubject(), acl.getMode(), acl.getPrincipal()); });

            tacls.forEach(a -> { Acl acl = toAcl((VncMap)a);
                                 setTopicAcl(acl.getSubject(), acl.getMode(), acl.getPrincipal()); });

            facls.forEach(a -> { Acl acl = toAcl((VncMap)a);
                                 setFunctionAcl(acl.getSubject(), acl.getMode(), acl.getPrincipal()); });

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

        final List<VncVal> facls = toAclList(functionAcls)
                                    .stream()
                                    .map(a->toVncMap(a))
                                    .collect(Collectors.toList());

        final VncHashMap data = VncHashMap.of(
                                    new VncString("authorizations"),
                                    VncList.ofColl(auths),

                                    new VncString("queue-default-acl"),
                                    toVncMap(queueDefault.get()),
                                    new VncString("topic-default-acl"),
                                    toVncMap(topicDefault.get()),
                                    new VncString("function-default-acl"),
                                    toVncMap(functionDefault.get()),

                                    new VncString("queue-acls"),
                                    VncList.ofColl(qacls),
                                    new VncString("topic-acls"),
                                    VncList.ofColl(tacls),
                                    new VncString("function-acls"),
                                    VncList.ofColl(facls));

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

    // ACL defaults
    private final AtomicReference<Acl> queueDefault    = new AtomicReference<>(new Acl("*", "*", AccessMode.READ_WRITE));
    private final AtomicReference<Acl> topicDefault    = new AtomicReference<>(new Acl("*", "*", AccessMode.READ_WRITE));
    private final AtomicReference<Acl> functionDefault = new AtomicReference<>(new Acl("*", "*", AccessMode.EXECUTE));

    // Mapped by principal -> Auth
    private final ConcurrentHashMap<String, Auth> authorizations = new ConcurrentHashMap<>();

    // Mapped by queueName -> principal -> ACL
    private final ConcurrentHashMap<String, Map<String, Acl>> queueAcls = new ConcurrentHashMap<>();

    // Mapped by topicName -> principal -> ACL
    private final ConcurrentHashMap<String, Map<String, Acl>> topicAcls = new ConcurrentHashMap<>();

    // Mapped by functionName -> principal -> ACL
    private final ConcurrentHashMap<String, Map<String, Acl>> functionAcls = new ConcurrentHashMap<>();
}
