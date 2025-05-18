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

import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncChar;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncMutableMap;


public class GenericMBean implements DynamicMBean {

    public GenericMBean(final VncMutableMap map) {
        this.map = map;
    }

    @Override
    public Object getAttribute(final String attribute)
    throws AttributeNotFoundException, MBeanException, ReflectionException {
        return map.get(new VncKeyword(attribute));
    }

    @Override
    public void setAttribute(final Attribute attribute)
    throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        map.assoc(
                new VncKeyword(attribute.getName(),
                JavaInteropUtil.convertToVncVal(attribute.getValue())));
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        final AttributeList list = new AttributeList();

        for(String attr : attributes) {
            list.add(new Attribute(attr, map.get(new VncKeyword(attr))));
        }

        return list;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        final List<String> attributeNames = new ArrayList<>();

        attributes.asList().forEach(a -> {
            attributeNames.add(a.getName());
            map.assoc(
                    new VncKeyword(a.getName()),
                    JavaInteropUtil.convertToVncVal(a.getValue()));
        });

        return getAttributes(attributeNames.toArray(new String[] {}));
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        throw new MBeanException(
                new RuntimeException("Dynamic MBean operations are not implemented!"),
                "Dynamic MBean operations are not implemented!");
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

    private MBeanAttributeInfo[] getMBeanAttributeInfo() {
        final List<MBeanAttributeInfo> infos = new ArrayList<>();
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
    	if (val instanceof VncInteger) return "int";
    	else if (val instanceof VncLong) return "long";
    	else if (val instanceof VncBoolean) return "boolean";
    	else if (val instanceof VncString) return "java.lang.String";
    	else if (val instanceof VncChar) return "java.lang.String";
    	else return val.getClass().getName();
    }



    private final VncMutableMap map;
}
