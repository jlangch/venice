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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.impl.util.mbean.GenericMBean;

public class MBeanFunctions {

    public static VncFunction mbean_platform_mbean_server =
        new VncFunction(
                "mbean/platform-mbean-server",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/platform-mbean-server)")
                    .doc(
                        "Returns the Java platform MBean server")
                    .examples(
                        "(mbean/platform-mbean-server)")
                    .seeAlso(
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister",
                        "mbean/operating-system-mxbean",
                        "mbean/runtime-mxbean",
                        "mbean/memory-mxbean")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                return new VncJavaObject(mbs);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_create_jmx_connection =
        new VncFunction(
                "mbean/create-jmx-connection",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/create-jmx-connection url)",
                        "(mbean/create-jmx-connection url env)")
                    .doc(
                        "Create a connection to a local or remote JMX MBean server.   \n\n" +
                        "Returns :javax.management.MBeanServerConnection object.      \n\n" +
                        "Prefer the macro `with-jmx-connection` to communicate with   \n" +
                        "a remote JMX server!")
                    .examples(
                        ";; without SSL and authentication                                         \n" +
                        "(mbean/create-jmx-connection                                              \n" +
                        "         \"service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi\"          \n" +
                        "         nil                                                              \n" +
                        "         nil)                                                             ",
                        ";; with SSL and username/password authentication                          \n" +
                        "(mbean/create-jmx-connection                                              \n" +
                        "         \"service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi\"          \n" +
                        "         { \"javax.net.ssl\" true                                         \n" +
                        "           \"javax.net.ssl.trustStore\" \"/path/to/jmx.truststore\"       \n" +
                        "           \"javax.net.ssl.trustStorePassword\" \"changeit\" }            \n" +
                        "         { \"jmx.remote.credentials\" [\"username\" \"password\"] })      ")
                    .seeAlso("with-jmx-connection")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final String url = Coerce.toVncString(args.first()).getValue();

                final VncMap sslMap = args.second() == Constants.Nil
                                        ? VncHashMap.empty()
                                        : Coerce.toVncMap(args.second());

                final VncMap envMap = args.third() == Constants.Nil
                                        ? VncHashMap.empty()
                                        : Coerce.toVncMap(args.third());

                // ssl
                final VncVal ssl_ = sslMap.get(new VncString("javax.net.ssl"));
                final VncVal trustStore_ = sslMap.get(new VncString("javax.net.ssl.trustStore"));
                final VncVal trustStorePwd_ = sslMap.get(new VncString("javax.net.ssl.trustStorePassword"));
                final boolean ssl = VncBoolean.isTrue(ssl_);
                final String trustStore = Types.isVncString(trustStore_)
                                            ? StringUtil.trimToNull(((VncString)trustStore_).getValue())
                                            : null;
                final String trustStorePwd = Types.isVncString(trustStorePwd_)
                                                ? StringUtil.trimToNull(((VncString)trustStorePwd_).getValue())
                                                : null;

                // environment
                final Map<String, Object> environment = new HashMap<>();
                if (!envMap.isEmpty()) {
                    envMap.getJavaMap().forEach((k,v) -> {
                        if (Types.isVncString(k)) {
                            final String key = ((VncString)k).getValue();

                            // special case for credentials
                            if (key.equals("jmx.remote.credentials")) {
                                final VncSequence seq = Coerce.toVncSequence(v);
                                final String user = Coerce.toVncString(seq.first()).getValue();
                                final String password = Coerce.toVncString(seq.second()).getValue();
                                environment.put(key, new String[] {user, password});
                            }
                            else {
                                final Object val = v.convertToJavaObject();
                                environment.put(key, val);
                            }
                        }
                        else {
                            throw new VncException("Invalid environment key. Must be a string");
                        }
                    });
                }

                try {
                    // Set SSL system properties
                    if (ssl) {
                        if (trustStore != null && trustStorePwd != null) {
                            System.setProperty("javax.net.ssl.trustStore", trustStore);
                            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);
                        }
                        environment.put("com.sun.jndi.rmi.factory.socket", new javax.rmi.ssl.SslRMIClientSocketFactory());
                    }

                    final JMXServiceURL jmxurl = new JMXServiceURL(url);
                    final JMXConnector jmxc = JMXConnectorFactory.connect(jmxurl, environment);
                    return new VncJavaObject(jmxc.getMBeanServerConnection());
                }
                catch(Exception ex) {
                    throw new VncException("Failed to create JMX server connection", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_jmx_connector_server_start =
        new VncFunction(
                "mbean/jmx-connector-server-start",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/jmx-connector-server-start port)")
                    .doc(
                        "Start a JMX connector server on a given port.                      \n\n" +
                        "A connector server is required if the MBeans should be accessible  \n" +
                        "from a remote Java VM.                                             \n\n" +
                        "It is strongly recommended to configure SSL and authentication for \n" +
                        "the JMX connector!")
                    .examples(
                        "(let [registry (mbean/jmx-connector-server-start 9999)] \n" +
                        "  (mbean/jmx-connector-server-alive? registry)          \n" +
                        "  (mbean/jmx-connector-server-stop registry))           ")
                    .seeAlso(
                        "mbean/jmx-connector-server-stop",
                        "mbean/jmx-connector-server-alive?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final long port = Coerce.toVncLong(args.first()).getValue();

                try {
                    LocateRegistry.createRegistry((int)port);

                    final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                    // Create JMXServiceURL and start connector server
                    final String url = "service:jmx:rmi:///jndi/rmi://localhost:" + port +"/jmxrmi";
                    final JMXServiceURL jmxurl = new JMXServiceURL(url);
                    final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(jmxurl, null, mbs);
                    cs.start();

                    return new VncJavaObject(cs);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to create JMX connector server", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_jmx_connector_server_stop =
        new VncFunction(
                "mbean/jmx-connector-server-stop",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/jmx-connector-server-stop server)")
                    .doc(
                        "Stop a JMX connector server")
                    .examples(
                        "(let [cs (mbean/jmx-connector-server-start 9999)] \n" +
                        "  (mbean/jmx-connector-server-alive? cs)          \n" +
                        "  (mbean/jmx-connector-server-stop cs))           ")
                    .seeAlso(
                        "mbean/jmx-connector-server-start",
                        "mbean/jmx-connector-server-alive?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final JMXConnectorServer cs = Coerce.toVncJavaObject(args.first(), JMXConnectorServer.class);

                try {
                    cs.stop();
                    return Nil;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to stop a JMX connector server", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_jmx_connector_server_alive_Q =
        new VncFunction(
                "mbean/jmx-connector-server-alive?",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/jmx-connector-server-alive? server)")
                    .doc(
                        "Returns true if the JMX connector server is running else false.")
                    .examples(
                        "(let [cs (mbean/jmx-connector-server-start 9999)] \n" +
                        "  (mbean/jmx-connector-server-alive? cs)          \n" +
                        "  (mbean/jmx-connector-server-stop cs))           ")
                    .seeAlso(
                        "mbean/jmx-connector-server-start",
                        "mbean/jmx-connector-server-stop")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final JMXConnectorServer cs = Coerce.toVncJavaObject(args.first(), JMXConnectorServer.class);

                try {
                    return VncBoolean.of(cs.isActive());
                }
                catch(Exception ex) {
                    throw new VncException("Failed to check if a JMX connector server is alive", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_query_mbean_object_names =
        new VncFunction(
                "mbean/query-mbean-object-names",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/query-mbean-object-names)")
                    .doc(
                        "Returns a list of the registered Java MBean object names.")
                    .examples(
                        "(mbean/query-mbean-object-names)")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                try {
                    final MBeanServerConnection mbs = getMBeanServerConnection();

                    final Set<ObjectName> mbeans = mbs.queryNames(null, null);

                    final List<VncVal> list = new ArrayList<>();

                    mbeans.forEach(on -> {
                        VncMap map = VncHashMap.empty();
                        map = map.assoc(new VncKeyword("canonical-name"), new VncString(on.getCanonicalName()));
                        map = map.assoc(new VncKeyword("domain"), new VncString(on.getDomain()));

                        VncMap props = VncHashMap.empty();
                        for(Entry<String,String> e : on.getKeyPropertyList().entrySet()) {
                            props = props.assoc(new VncString(e.getKey()), new VncString(e.getValue()));
                        }
                        map = map.assoc(new VncKeyword("properties"), props);
                        map = map.assoc(new VncKeyword("object-name"), new VncJavaObject(on));

                        list.add(map);
                     });

                    return VncList.ofColl(list);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to query MBeans", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_object_name =
        new VncFunction(
                "mbean/object-name",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/object-name name)",
                        "(mbean/object-name domain key value)")
                    .doc(
                        "Creates a Java MBean object name")
                    .examples(
                        "(mbean/object-name \"java.lang:type=OperatingSystem\")",
                        "(mbean/object-name \"java.lang\" \"type\" \"OperatingSystem\")")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                   .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 3);

                if (args.size() == 1) {
                    final String name = Coerce.toVncString(args.first()).getValue();
                    try {
                        return new VncJavaObject(ObjectName.getInstance(name));
                    }
                    catch(MalformedObjectNameException ex) {
                        throw new VncException("Malformed MBean object name \"" + name + "\"", ex);
                    }
                }
                else {
                    final String domain = Coerce.toVncString(args.first()).getValue();
                    final String key = Coerce.toVncString(args.second()).getValue();
                    final String value = Coerce.toVncString(args.third()).getValue();
                    try {
                          return new VncJavaObject(ObjectName.getInstance(domain, key, value));
                    }
                    catch(MalformedObjectNameException ex) {
                        throw new VncException(
                                "Malformed MBean object name. " +
                                "domain: \"" + domain + "\", " +
                                "key: \"" + key + "\", " +
                                "value: \"" + value + "\"" +
                                ex);
                    }
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_info =
        new VncFunction(
                "mbean/info",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/info object-name)")
                    .doc(
                        "Return the MBean info of a Java MBean.                                       \n" +
                        "                                                                             \n" +
                        "Example MBean:                                                               \n" +
                        "                                                                             \n" +
                        "```                                                                          \n" +
                        "public interface HelloMBean {                                                \n" +
                        "    void sayHello();                                                         \n" +
                        "    int add(int x, int y);                                                   \n" +
                        "    int getMaxCount();                                                       \n" +
                        "    void setMaxCount(int c);                                                 \n" +
                        "}                                                                            \n" +
                        "```                                                                          \n" +
                        "                                                                             \n" +
                        "Example MBean info:                                                                     \n" +
                        "                                                                                        \n" +
                        "```                                                                                     \n" +
                        "{:classname \"com.github.jlangch.venice.demo.mbean.Hello\"                              \n" +
                        " :notifications {}                                                                      \n" +
                        " :operations {:add {:parameters {:p1 {:descriptor {}                                    \n" +
                        "                                      :type \"int\"                                     \n" +
                        "                                      :description \"\"}                                \n" +
                        "                                 :p2 {:descriptor {}                                    \n" +
                        "                                      :type \"int\"                                     \n" +
                        "                                      :description \"\"}}                               \n" +
                        "                    :descriptor {}                                                      \n" +
                        "                    :return-type \"int\"                                                \n" +
                        "                    :description \"Operation exposed for management\"}                  \n" +
                        "              :sayHello {:parameters {}                                                 \n" +
                        "                         :descriptor {}                                                 \n" +
                        "                         :return-type \"void\"                                          \n" +
                        "                         :description \"Operation exposed for management\"}}            \n" +
                        " :attributes {:MaxCount {:descriptor {}                                                 \n" +
                        "                         :type \"int\"                                                  \n" +
                        "                         :description \"Attribute exposed for management\"}}            \n" +
                        " :constructors {:com.github.jlangch.venice.demo.mbean.Hello                             \n" +
                        "                   {:parameters {}                                                      \n" +
                        "                    :descriptor {}                                                      \n" +
                        "                    :description \"Public constructor of the MBean\"}}                  \n" +
                        "                :description \"Information on the management interface of the MBean\"}  ")
                    .examples(
                        "(let [name (mbean/object-name \"java.lang:type=OperatingSystem\")]  \n" +
                        "  (mbean/info name))                                                ",
                        "(do                                                         \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)      \n" +
                        "  (let [bean (. :Hello :new)                                \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]     \n" +
                        "     (mbean/register bean name)                             \n" +
                        "     (mbean/info name)))                                    ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ObjectName name = Coerce.toVncJavaObject(args.first(), ObjectName.class);

                try {
                    final MBeanServerConnection mbs = getMBeanServerConnection();

                    final MBeanInfo info = mbs.getMBeanInfo(name);

                    VncMap map = VncHashMap.empty();

                    // main
                    map = map.assoc(new VncKeyword("classname"),   new VncString(info.getClassName()));
                    map = map.assoc(new VncKeyword("description"), new VncString(info.getDescription()));

                    // constructors
                    VncMap ctorMap = VncHashMap.empty();
                    final MBeanConstructorInfo[] constructors = info.getConstructors();
                    for(MBeanConstructorInfo ctor : constructors) {
                        VncMap c_ = VncHashMap.empty();
                        c_ = c_.assoc(new VncKeyword("description"), new VncString(ctor.getDescription()));
                        c_ = c_.assoc(new VncKeyword("descriptor"),  new VncJavaObject(ctor.getDescriptor()));
                        c_ = c_.assoc(new VncKeyword("parameters"),  mapMBeanParameterInfo(ctor.getSignature()));
                        ctorMap = ctorMap.assoc(new VncKeyword(ctor.getName()), c_);
                    }
                    map = map.assoc(new VncKeyword("constructors"), ctorMap);

                    // attributes
                    VncMap attrMap = VncHashMap.empty();
                    final MBeanAttributeInfo[] attributes = info.getAttributes();
                    for(MBeanAttributeInfo attr : attributes) {
                        VncMap a_ = VncHashMap.empty();
                        a_ = a_.assoc(new VncKeyword("description"), new VncString(attr.getDescription()));
                        a_ = a_.assoc(new VncKeyword("type"),        new VncString(attr.getType()));
                        a_ = a_.assoc(new VncKeyword("descriptor"),  new VncJavaObject(attr.getDescriptor()));
                        attrMap = attrMap.assoc(new VncKeyword(attr.getName()), a_);
                    }
                    map = map.assoc(new VncKeyword("attributes"), attrMap);

                    // operations
                    VncMap opMap = VncHashMap.empty();
                    final MBeanOperationInfo[] operations = info.getOperations();
                    for(MBeanOperationInfo op : operations) {
                        VncMap o_ = VncHashMap.empty();
                        o_ = o_.assoc(new VncKeyword("description"), new VncString(op.getDescription()));
                        o_ = o_.assoc(new VncKeyword("return-type"), new VncString(op.getReturnType()));
                        o_ = o_.assoc(new VncKeyword("descriptor"),  new VncJavaObject(op.getDescriptor()));
                        o_ = o_.assoc(new VncKeyword("parameters"),  mapMBeanParameterInfo(op.getSignature()));
                        opMap = opMap.assoc(new VncKeyword(op.getName()), o_);
                    }
                    map = map.assoc(new VncKeyword("operations"), opMap);

                    // notifications
                    VncMap notifMap = VncHashMap.empty();
                    final MBeanNotificationInfo[] notifications = info.getNotifications();
                    for(MBeanNotificationInfo notif : notifications) {
                        VncMap n_ = VncHashMap.empty();
                        n_ = n_.assoc(new VncKeyword("description"), new VncString(notif.getDescription()));
                        n_ = n_.assoc(new VncKeyword("notif-types"), JavaInteropUtil.convertToVncVal(
                                                                            Arrays.asList(notif.getNotifTypes())));
                        n_ = n_.assoc(new VncKeyword("descriptor"),  new VncJavaObject(notif.getDescriptor()));
                        notifMap = notifMap.assoc(new VncKeyword(notif.getName()), n_);
                    }
                    map = map.assoc(new VncKeyword("notifications"), notifMap);

                    return map;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get MBean info", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_attribute =
        new VncFunction(
                "mbean/attribute",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/attribute object-name attribute-name)")
                    .doc(
                        "Returns the value of a Java MBean attribute.             \n" +
                        "                                                         \n" +
                        "```                                                      \n" +
                        "// Java MBean example                                    \n" +
                        "public interface HelloMBean {                            \n" +
                        "    int getMaxCount();                                   \n" +
                        "    void setMaxCount(int c);                             \n" +
                        "}                                                        \n" +
                        "                                                         \n" +
                        "public class Hello implements HelloMBean {               \n" +
                        "    @Override                                            \n" +
                        "    public int getMaxCount() {                           \n" +
                        "       return maxCount;                                  \n" +
                        "    }                                                    \n" +
                        "                                                         \n" +
                        "    @Override                                            \n" +
                        "    public coid setMaxCount(int c) {                     \n" +
                        "       maxCount = c;                                     \n" +
                        "    }                                                    \n" +
                        "                                                         \n" +
                        "    private int maxCount = 42;                           \n" +
                        "}                                                        \n" +
                        "```                                                      ")
                    .examples(
                        "(-> (mbean/object-name \"java.lang:type=OperatingSystem\")  \n" +
                        "    (mbean/attribute :ProcessCpuLoad))                      ",
                        "(-> (mbean/object-name \"java.lang:type=OperatingSystem\")  \n" +
                        "    (mbean/attribute :SystemCpuLoad))                       ",
                        ";; static MBean                                             \n" +
                        "(do                                                         \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)      \n" +
                        "  (let [bean (. :Hello :new)                                \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]     \n" +
                        "     (mbean/register bean name)                             \n" +
                        "     (mbean/attribute name :MaxCount)))                     ",
                        ";; dynamic MBean                                            \n" +
                        "(do                                                         \n" +
                        "  (let [bean (atom (hash-map :count 10))                    \n" +
                        "        name (mbean/object-name \"venice:type=Data\")]      \n" +
                        "    (mbean/register-dynamic bean name)                      \n" +
                        "    (mbean/attribute name :count)))                         ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ObjectName objectName = Coerce.toVncJavaObject(args.first(), ObjectName.class);
                final String attributeName = Coerce.toVncKeyword(args.second()).getSimpleName();

                try {
                    final MBeanServerConnection mbs = getMBeanServerConnection();

                    final Object val = mbs.getAttribute(objectName, attributeName);

                    return JavaInteropUtil.convertToVncVal(val);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get MBean attribute value", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_attribute_BANG =
        new VncFunction(
                "mbean/attribute!",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/attribute! object-name attribute-name attribute-value)")
                    .doc(
                        "Set the value of a Java MBean attribute.")
                    .examples(
                        ";; static MBean                                              \n" +
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)       \n" +
                        "  (let [bean (. :Hello :new)                                 \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "     (mbean/register bean name)                              \n" +
                        "     (mbean/attribute! name :MaxCount 64I)                   \n" +
                        "     (mbean/attribute name :MaxCount)))                      ",
                        ";; dynamic MBean                                             \n" +
                        "(do                                                          \n" +
                        "  (let [bean (atom (hash-map :count 10))                     \n" +
                        "        name (mbean/object-name \"venice:type=Data\")]       \n" +
                        "    (mbean/register-dynamic bean name)                       \n" +
                        "    (mbean/attribute! name :count 20)                        \n" +
                        "    (mbean/attribute name :count)))                          ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3);

                final ObjectName objectName = Coerce.toVncJavaObject(args.first(), ObjectName.class);
                final String attributeName = Coerce.toVncKeyword(args.second()).getSimpleName();
                final VncVal attributeValue = args.third();

                try {
                    final MBeanServerConnection mbs = getMBeanServerConnection();

                    final AttributeList list = new AttributeList();
                    list.add(new Attribute(attributeName, attributeValue.convertToJavaObject()));

                    final AttributeList result = mbs.setAttributes(objectName, list);

                    VncMap vncResult = new VncHashMap();
                    for(int ii=0; ii<result.size(); ii++) {
                        final Attribute a = (Attribute)result.get(ii);
                        vncResult = vncResult.assoc(
                                        new VncKeyword(a.getName()),
                                        JavaInteropUtil.convertToVncVal(a.getValue()));
                    }
                    return vncResult;
                }
                catch(Exception ex) {
                    throw new VncException("Failed to set MBean attribute value", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_invoke =
        new VncFunction(
                "mbean/invoke",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/invoke object-name operation params)",
                        "(mbean/invoke object-name operation params signature)")
                    .doc(
                        "Invoke a Java MBean operation and return its result      \n" +
                        "value.                                                   \n" +
                        "                                                         \n" +
                        "Supported for static MBeans only!                        \n" +
                        "                                                         \n" +
                        "```                                                      \n" +
                        "// Java MBean example                                    \n" +
                        "public interface HelloMBean {                            \n" +
                        "    void sayHello();                                     \n" +
                        "    int add(int x, int y);                               \n" +
                        "}                                                        \n" +
                        "                                                         \n" +
                        "public class Hello implements HelloMBean {               \n" +
                        "    @Override                                            \n" +
                        "    public void sayHello() {                             \n" +
                        "       System.out.println(\"Hello, world!\");            \n" +
                        "    }                                                    \n" +
                        "                                                         \n" +
                        "    @Override                                            \n" +
                        "    public int add(int x, int y) {                       \n" +
                        "        return x + y;                                    \n" +
                        "    }                                                    \n" +
                        "}                                                        \n" +
                        "```                                                      ")
                    .examples(
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)       \n" +
                        "  (let [bean (. :Hello :new)                                 \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "    (mbean/register bean name)                               \n" +
                        "    ;; use an explicit operation signature                   \n" +
                        "    (mbean/invoke name :add [1I 2I] [\"int\" \"int\"])))     ",
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)       \n" +
                        "  (let [bean (. :Hello :new)                                 \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "    (mbean/register bean name)                               \n" +
                        "    ;; use the :add operation signature from the MBeanInfo   \n" +
                        "    (mbean/invoke name :add [1I 2I])))                       ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/register",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 3, 4);

                final ObjectName name = Coerce.toVncJavaObject(args.first(), ObjectName.class);
                final String operation = Coerce.toVncKeyword(args.second()).getSimpleName();
                final VncSequence vncParams = Coerce.toVncSequence(args.third());

                // Build params
                final Object[] params = new Object[vncParams.size()];
                for (int ii=0; ii<params.length; ii++) {
                    params[ii] = vncParams.nth(ii).convertToJavaObject();
                }

                try {
                    final MBeanServerConnection mbs = getMBeanServerConnection();

                    final MBeanInfo info = mbs.getMBeanInfo(name);

                    final List<String> signature = new ArrayList<>();

                    if (args.size() == 3) {
                        // Find the correct signature from the MBeanInfo
                        MBeanOperationInfo mbeanOp = null;
                        for (MBeanOperationInfo op : info.getOperations()) {
                            if (op.getName().equals(operation)) {
                                mbeanOp = op;
                                break;
                            }
                        }

                        // Build signature from MBeanOperationInfo
                        for (int ii=0; ii<mbeanOp.getSignature().length; ii++) {
                            signature.add(mbeanOp.getSignature()[ii].getType());
                        }
                    }
                    else {
                        final VncSequence vncSignature = Coerce.toVncSequence(args.fourth());

                        // Build signature function args
                        for(int ii=0; ii<vncSignature.size(); ii++) {
                            final Object s = vncSignature.nth(ii).convertToJavaObject();
                            if (s instanceof String) {
                                signature.add((String)s);
                            }
                            else {
                                throw new VncException(
                                        "The signature element " + ii + " must be of type string");
                            }
                        };
                    }

                    // Invoke
                    final Object result = mbs.invoke(
                                            name,
                                            operation,
                                            params,
                                            signature.toArray(new String[] {}));

                    return JavaInteropUtil.convertToVncVal(result);
                }
                catch(InstanceNotFoundException ex) {
                    throw new VncException("MBean not found", ex);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to invoke MBean", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_register =
        new VncFunction(
                "mbean/register",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/register mbean name)")
                    .doc(
                        "Register a Java MBean")
                    .examples(
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)       \n" +
                        "  (let [bean (. :Hello :new)                                 \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "     (mbean/register bean name)))                            ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register-dynamic",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Object mbean = Coerce.toVncJavaObject(args.first(), Object.class);
                final ObjectName name = Coerce.toVncJavaObject(args.second(), ObjectName.class);

                try {
                    final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                    final ObjectInstance instance = mbs.registerMBean(mbean, name);

                    return new VncJavaObject(instance);
                }
                catch(InstanceAlreadyExistsException ex) {
                    throw new VncException(
                            "Failed to register MBean. The MBean is already registered!", ex);
                }
                catch(MBeanRegistrationException ex) {
                    throw new VncException(
                            "Failed to register MBean!", ex);
                }
                catch(NotCompliantMBeanException ex) {
                    throw new VncException(
                            "Failed to register MBean. The MBean is not a compliant " +
                            "bean according to the MBean specification!",
                            ex);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to register MBean", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_register_dynamic =
        new VncFunction(
                "mbean/register-dynamic",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/register-dynamic bean name)")
                    .doc(
                        "Register a bean (a map wrapped by an atom or a volatile) " +
                        "as a dynamic Java MBean.")
                    .examples(
                        "(do                                                         \n" +
                        "  (let [bean (atom {:count 10})                             \n" +
                        "        name (mbean/object-name \"venice:type=Data\")]      \n" +
                        "    (mbean/register-dynamic bean name)                      \n" +
                        "    (println \":count  \" (mbean/attribute name :count))    \n" +
                        "    (mbean/attribute! name :count 20)                       \n" +
                        "    (println \":count  \" (mbean/attribute name :count))))  ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/attribute!",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final VncVal mbean = args.first();
                if (!(mbean instanceof IDeref)) {
                    throw new VncException(
                            "Failed to register MBean. " +
                            "A dynamic bean must be map wrapped by an atom or a volatile!");
                }
                final ObjectName name = Coerce.toVncJavaObject(args.second(), ObjectName.class);

                try {
                    final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                    final ObjectInstance instance = mbs.registerMBean(new GenericMBean((IDeref)mbean), name);

                    return new VncJavaObject(instance);
                }
                catch(InstanceAlreadyExistsException ex) {
                    throw new VncException(
                            "Failed to register MBean. The MBean is already registered!", ex);
                }
                catch(MBeanRegistrationException ex) {
                    throw new VncException(
                            "Failed to register MBean!", ex);
                }
                catch(NotCompliantMBeanException ex) {
                    throw new VncException(
                            "Failed to register MBean. The MBean is not a compliant " +
                            "bean according to the MBean specification!",
                            ex);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to register MBean", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_unregister =
        new VncFunction(
                "mbean/unregister",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/unregister name)")
                    .doc(
                        "Unregister a Java MBean")
                    .examples(
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.demo.mbean.Hello)       \n" +
                        "  (let [bean (. :Hello :new)                                 \n" +
                        "        name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "     (mbean/register bean name)                              \n" +
                        "     (mbean/unregister name)))                               ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/object-name",
                        "mbean/register",
                        "mbean/register-dynamic")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ObjectName name = Coerce.toVncJavaObject(args.first(), ObjectName.class);

                try {
                    final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                    mbs.unregisterMBean(name);

                    return Nil;
                }
                catch(InstanceNotFoundException ex) {
                    throw new VncException(
                            "Failed to unregister MBean. The MBean does not exist!", ex);
                }
                catch(MBeanRegistrationException ex) {
                    throw new VncException(
                            "Failed to unregister MBean!", ex);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to unregister MBean", ex);
                }
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_operating_system_mxbean =
        new VncFunction(
                "mbean/operating-system-mxbean",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/operating-system-mxbean)")
                    .doc(
                        "Returns the Java Operating System MXBean")
                    .examples(
                        "(mbean/operating-system-mxbean)")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/runtime-mxbean",
                        "mbean/memory-mxbean")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final OperatingSystemMXBean mxbean = ManagementFactory.getOperatingSystemMXBean();
                return new VncJavaObject(mxbean);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_runtime_mxbean =
        new VncFunction(
                "mbean/runtime-mxbean",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/runtime-mxbean)")
                    .doc(
                        "Returns the Java Runtime MXBean")
                    .examples(
                        "(mbean/runtime-mxbean)")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/operating-system-mxbean",
                        "mbean/memory-mxbean")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
                return new VncJavaObject(mxbean);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction mbean_memory_mxbean =
        new VncFunction(
                "mbean/memory-mxbean",
                VncFunction
                    .meta()
                    .arglists(
                        "(mbean/memory-mxbean)")
                    .doc(
                        "Returns the Java Memory MXBean")
                    .examples(
                        "(mbean/memory-mxbean)")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/operating-system-mxbean",
                        "mbean/runtime-mxbean")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
                return new VncJavaObject(mxbean);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };



    private static VncMap mapMBeanParameterInfo(final MBeanParameterInfo[] paramInfo) {
        VncMap params = VncHashMap.empty();
        for(MBeanParameterInfo param :  paramInfo) {
            VncMap p = VncHashMap.empty();
            p = p.assoc(new VncKeyword("description"), new VncString(param.getDescription()));
            p = p.assoc(new VncKeyword("type"),        new VncString(param.getType()));
            p = p.assoc(new VncKeyword("descriptor"),  new VncJavaObject(param.getDescriptor()));
            params = params.assoc(new VncKeyword(param.getName()), p);
        }
        return params;
    }


    private static MBeanServerConnection getMBeanServerConnection() {
        final VncVal conn = ThreadContext.getValue(JMX_CONNECTION);

        if (Types.isVncJavaObject(conn)) {
            final Object delegate = ((VncJavaObject)conn).getDelegate();
            if (delegate instanceof MBeanServerConnection) {
                return (MBeanServerConnection)conn;
            }
        }

        // otherwise connect to local MBean Server
        return ManagementFactory.getPlatformMBeanServer();
    }



    private static final VncKeyword JMX_CONNECTION = new VncKeyword("*jmx-connection*");


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()
                    .add(mbean_platform_mbean_server)
                    .add(mbean_query_mbean_object_names)
                    .add(mbean_object_name)
                    .add(mbean_info)
                    .add(mbean_attribute)
                    .add(mbean_attribute_BANG)
                    .add(mbean_invoke)
                    .add(mbean_register)
                    .add(mbean_register_dynamic)
                    .add(mbean_unregister)
                    .add(mbean_operating_system_mxbean)
                    .add(mbean_runtime_mxbean)
                    .add(mbean_memory_mxbean)

                    // JMX
                    .add(mbean_create_jmx_connection)
                    .add(mbean_jmx_connector_server_start)
                    .add(mbean_jmx_connector_server_stop)
                    .add(mbean_jmx_connector_server_alive_Q)

                    .toMap();
}
