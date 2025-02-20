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
package com.github.jlangch.venice.impl.javainterop;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.util.StringUtil;


public class JavaImports implements Serializable {

    private JavaImports(final Map<String,String> imports) {
        this.imports.putAll(imports);
    }

    public JavaImports() {
        // from java.lang
        add(Throwable.class.getName());
        add(Exception.class.getName());
        add(RuntimeException.class.getName());
        add(NullPointerException.class.getName());
        add(IllegalArgumentException.class.getName());

        // from com.github.jlangch.venice
        add(com.github.jlangch.venice.VncException.class.getName());
        add(com.github.jlangch.venice.AssertionException.class.getName());
        add(com.github.jlangch.venice.ValueException.class.getName());
        add(com.github.jlangch.venice.SecurityException.class.getName());
    }


    /**
     * Looks up a class name
     *
     * @param simpleClassName A simple class name like 'Math'
     * @return the class name e.g.: 'java.lang.Math' or <code>null</code> if not found
     */
    public String lookupClassName(final String simpleClassName) {
        return imports.get(simpleClassName);
    }

    /**
     * Resolves a class name.
     *
     * @param className A simple class name like 'Math' or a class
     *                  'java.lang.Math'
     * @return the mapped class 'Math' -&gt; 'java.lang.Math' or the passed
     *         value if a mapping does nor exist
     */
    public String resolveClassName(final String className) {
        final String cn = imports.get(className);
        return cn == null ? className : cn;
    }

    public void add(final String clazz) {
        add(clazz, getSimpleClassname(clazz));
    }

    public void add(final String clazz, final String alias) {
        final String alias_ = StringUtil.trimToNull(alias);
        if (alias_ == null) {
            throw new VncException(String.format(
                    "An import class alias on class '%s' must not be blank!",
                    clazz));
        }

        validateNoDuplicateAlias(clazz, alias_);

        imports.put(alias_, clazz);
    }

    public void clear() {
        imports.clear();
    }

    public JavaImports copy() {
       return new JavaImports(imports);
    }

    public VncList list() {
        return VncList.ofColl(
                imports
                    .entrySet()
                    .stream()
                    .map(e -> new String[] {e.getValue(), e.getKey()})
                    .sorted(Comparator.comparing(e -> e[0]))
                    .map(i -> VncVector.of(
                                new VncKeyword(i[0]),
                                new VncKeyword(i[1])))
                    .collect(Collectors.toList()));
    }

    private String getSimpleClassname(final String clazz) {
        final int pos = clazz.lastIndexOf('.');
        return pos < 0 ? clazz : clazz.substring(pos+1);
    }

    private void validateNoDuplicateAlias(final String clazz, final String alias) {
        final String c = imports.get(alias);

        if (c != null && !c.equals(clazz)) {
            throw new VncException(String.format(
                    "Failed to import class '%s' with alias '%s'. The import alias "
                    + "already exists for another class.",
                    clazz, alias));
        }
    }


    private static final long serialVersionUID = 1784667662341909868L;

    private final Map<String,String> imports = new ConcurrentHashMap<>();
}
