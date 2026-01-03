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
package com.github.jlangch.venice.impl.util.mbean;

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.IDeref;
import com.github.jlangch.venice.impl.types.VncAtom;
import com.github.jlangch.venice.impl.types.VncBigDecimal;
import com.github.jlangch.venice.impl.types.VncBigInteger;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFloat;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.VncVolatile;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class GenericMBean implements DynamicMBean {

    public GenericMBean(final IDeref stateRef) {
        this.stateRef = stateRef;
    }

    @Override
    public Object getAttribute(final String attribute)
    throws AttributeNotFoundException, MBeanException, ReflectionException {
        final VncVal state = stateRef.deref();
        return state instanceof VncMap
                 ? getAttribute(state, attribute)
                 : null;
    }

    @Override
    public void setAttribute(final Attribute attribute)
    throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
         setAttribute(attribute.getName(), attribute.getValue());
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        final AttributeList list = new AttributeList();

        final VncVal state = stateRef.deref();
        if (state instanceof VncMap) {
            for(String attr : attributes) {
                list.add(new Attribute(attr, getAttribute(state, attr)));
            }
        }
        else {
             return null;
        }

        return list;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        final List<String> attributeNames = new ArrayList<>();

        attributes.asList().forEach(a -> {
            attributeNames.add(a.getName());
            setAttribute(a.getName(), a.getValue());
        });

        return getAttributes(attributeNames.toArray(new String[] {}));
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
    throws MBeanException, ReflectionException {
        final String msg = "Dynamic MBean operations are not implemented!";
        throw new MBeanException(new RuntimeException(msg), msg);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attributes = getMBeanAttributeInfo();

        return new MBeanInfo(
                getClass().getName(),     // class name
                "Venice Dynamic MBean",   // description
                attributes,               // attributes
                null,                     // constructors
                null,                     // operations
                null);                    // notifications
    }

    private Object getAttribute(final VncVal state, final String attribute) {
        return ((VncMap)state).get(new VncKeyword(attribute)).convertToJavaObject();
    }

    private void setAttribute(final String name, final Object value) {
         final VncKeyword key = new VncKeyword(name);
         final VncVal val = JavaInteropUtil.convertToVncVal(value);

         final VncMap update = VncHashMap.of(key, val);

        if (stateRef instanceof VncAtom) {
             ((VncAtom)stateRef).swap(CoreFunctions.merge, VncList.of(update));
        }
        else if (stateRef instanceof VncVolatile) {
             ((VncVolatile)stateRef).swap(CoreFunctions.merge, VncList.of(update));
        }
    }

    private MBeanAttributeInfo[] getMBeanAttributeInfo() {
        final List<MBeanAttributeInfo> infos = new ArrayList<>();
        final VncVal state = stateRef.deref();
        if (state instanceof VncMap) {
             final VncMap map = (VncMap)state;
            map.keys().forEach(k -> {
                if (k instanceof VncString) {
                    infos.add(getMBeanAttributeInfo(
                                   ((VncString)k).getValue(),
                                   map.get(k)));
                }
                else if (k instanceof VncKeyword) {
                    infos.add(getMBeanAttributeInfo(
                                   ((VncKeyword)k).getSimpleName(),
                                   map.get(k)));
                }
             });
        }

        return infos.toArray(new MBeanAttributeInfo[] {});
    }

    private MBeanAttributeInfo getMBeanAttributeInfo(
              final String name,
              final VncVal val
    ) {
        return new MBeanAttributeInfo(
                name,           // name
                guessType(val), // type,
                name,           // description,
                true,           // isReadable,
                true,           // isWritable,
                false);         // isIs
    }

    private String guessType(final VncVal val) {
         if (val instanceof VncInteger)         return "int";
         else if (val instanceof VncLong)       return "long";
         else if (val instanceof VncBoolean)    return "boolean";
         else if (val instanceof VncString)     return "java.lang.String";
         else if (val instanceof VncChar)       return "java.lang.String";
         else if (val instanceof VncDouble)     return "java.lang.Double";
         else if (val instanceof VncFloat)      return "java.lang.Float";
         else if (val instanceof VncBigDecimal) return "java.math.BigDecimal";
         else if (val instanceof VncBigInteger) return "java.math.BigInteger";

         else return val.getClass().getName();
    }


    private final IDeref stateRef;
}
