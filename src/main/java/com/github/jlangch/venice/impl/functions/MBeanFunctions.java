/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2025 Venice
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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.Constants;
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
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;


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
                        "mbean/invoke",
                        "mbean/register",
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
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
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
                        "mbean/invoke",
                        "mbean/register",
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
                        "Return the MBean info of a Java MBean")
                    .examples(
                        "(let [m (mbean/object-name \"java.lang:type=OperatingSystem\")]  \n" +
                        "  (mbean/info m))                                                ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/attribute",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ObjectName name = Coerce.toVncJavaObject(args.first(), ObjectName.class);

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                try {
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
                        "    void sayHello();                                     \n" +
                        "    int add(int x, int y);                               \n" +
                        "    int getFourtyTwo();                                  \n" +
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
                        "                                                         \n" +
                        "    @Override                                            \n" +
                        "    public int getFourtyTwo() {                          \n" +
                        "       return 42;                                        \n" +
                        "    }                                                    \n" +
                        "}                                                        \n" +
                        "```                                                      ")
                    .examples(
                        "(-> (mbean/object-name \"java.lang:type=OperatingSystem\")  \n" +
                        "    (mbean/attribute :ProcessCpuLoad))                      ",
                        "(-> (mbean/object-name \"java.lang:type=OperatingSystem\")  \n" +
                        "    (mbean/attribute :SystemCpuLoad))                       ",
                        "(do                                                         \n" +
                        "  (import :com.github.jlangch.venice.impl.util.mbean.Hello) \n" +
                        "  (let [name (mbean/object-name \"venice:type=Hello\")]     \n" +
                        "     (mbean/register (. :Hello :new) name)                  \n" +
                        "     (mbean/attribute name :FourtyTwo)))                    ")
                    .seeAlso(
                            "mbean/platform-mbean-server",
                            "mbean/query-mbean-object-names",
                            "mbean/object-name",
                            "mbean/info",
                            "mbean/invoke",
                            "mbean/register",
                            "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final ObjectName objectName = Coerce.toVncJavaObject(args.first(), ObjectName.class);
                final String attributeName = Coerce.toVncKeyword(args.second()).getSimpleName();

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                try {
                    final AttributeList list = mbs.getAttributes(
                                                        objectName,
                                                        new String[]{ attributeName });

                    final Object value = Optional
                                            .ofNullable(list)
                                            .map(l -> l.isEmpty() ? null : l)
                                            .map(List::iterator)
                                            .map(Iterator::next)
                                            .map(Attribute.class::cast)
                                            .map(Attribute::getValue)
                                            .orElse(null);

                    return JavaInteropUtil.convertToVncVal(value);
                }
                catch(Exception ex) {
                    throw new VncException("Failed to get MBean attribute value", ex);
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
                        "```                                                      \n" +
                        "// Java MBean example                                    \n" +
                        "public interface HelloMBean {                            \n" +
                        "    void sayHello();                                     \n" +
                        "    int add(int x, int y);                               \n" +
                        "    int getFourtyTwo();                                  \n" +
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
                        "                                                         \n" +
                        "    @Override                                            \n" +
                        "    public int getFourtyTwo() {                          \n" +
                        "       return 42;                                        \n" +
                        "    }                                                    \n" +
                        "}                                                        \n" +
                        "```                                                      ")
                    .examples(
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                        "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "    (mbean/register (. :Hello :new) name)                    \n" +
                        "    ;; use an explicit operation signature                   \n" +
                        "    (mbean/invoke name :add [1I 2I] [\"int\" \"int\"])))     ",
                        "(do                                                          \n" +
                        "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                        "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "    (mbean/register (. :Hello :new) name)                    \n" +
                        "    ;; use the :add operation signature from the MBeanInfo   \n" +
                        "    (mbean/invoke name :add [1I 2I])))                       ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/register",
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

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                try {
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
                    throw new VncException("Failed to invokeMBean", ex);
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
                        "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                        "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "     (mbean/register (. :Hello :new) name)))                 ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/query-mbean-object-names",
                        "mbean/object-name",
                        "mbean/info",
                        "mbean/attribute",
                        "mbean/invoke",
                        "mbean/register",
                        "mbean/unregister")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 2);

                final Object mbean = Coerce.toVncJavaObject(args.first(), Object.class);
                final ObjectName name = Coerce.toVncJavaObject(args.second(), ObjectName.class);

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                try {
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
                        "  (import :com.github.jlangch.venice.impl.util.mbean.Hello)  \n" +
                        "  (let [name (mbean/object-name \"venice:type=Hello\")]      \n" +
                        "     (mbean/register (. :Hello :new) name)                   \n" +
                        "     (mbean/unregister name)))                               ")
                    .seeAlso(
                        "mbean/platform-mbean-server",
                        "mbean/object-name",
                        "mbean/register")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1);

                final ObjectName name = Coerce.toVncJavaObject(args.first(), ObjectName.class);

                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                try {
                    mbs.unregisterMBean(name);

                    return Constants.Nil;
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
                    .add(mbean_invoke)
                    .add(mbean_register)
                    .add(mbean_unregister)
                    .add(mbean_operating_system_mxbean)
                    .add(mbean_runtime_mxbean)
                    .add(mbean_memory_mxbean)

                    .toMap();
}
