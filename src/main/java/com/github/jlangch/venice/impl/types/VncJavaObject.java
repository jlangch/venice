/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.functions.ArrayFunctions;
import com.github.jlangch.venice.impl.javainterop.Invoker;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncMapEntry;
import com.github.jlangch.venice.impl.types.collections.VncSequence;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.reflect.ReflectionAccessor;
import com.github.jlangch.venice.impl.util.reflect.ReflectionTypes;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.IInvoker;
import com.github.jlangch.venice.javainterop.ReturnValue;


public class VncJavaObject extends VncMap implements IVncJavaObject {

    public VncJavaObject(final Object obj) {
        this(obj, null, Constants.Nil);
    }

    public VncJavaObject(final Object obj, final VncVal meta) {
        this(obj, null, meta);
    }

    private VncJavaObject(final Object obj, final Class<?> formalType, final VncVal meta) {
        super(meta);

        this.delegate = obj instanceof VncVal ? ((VncVal)obj).convertToJavaObject() : obj;
        this.delegateFormalType = formalType;
    }


    public static VncJavaObject from(final ReturnValue val) {
        return new VncJavaObject(val.getValue(), val.getFormalType(), Constants.Nil);
    }

    public static VncJavaObject from(final Object val, final Class<?> formalType) {
        return new VncJavaObject(val, formalType, Constants.Nil);
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }

    @Override
    public Class<?> getDelegateFormalType() {
        return delegateFormalType;
    }

    @Override
    public VncMap emptyWithMeta() {
        throw new VncException("VncJavaObject::emptyWithMeta() is not supported");
    }
    @Override
    public VncHashMap withValues(final Map<VncVal,VncVal> replaceVals) {
        throw new VncException("VncJavaObject::withValues() is not supported");
    }

    @Override
    public VncHashMap withValues(
            final Map<VncVal,VncVal> replaceVals,
            final VncVal meta
    ) {
        throw new VncException("VncJavaObject::withValues() is not supported");
    }

    @Override
    public VncMap withMeta(final VncVal meta) {
        return new VncJavaObject(delegate, meta);
    }

    @Override
    public VncKeyword getType() {
        final Class<?> type = delegate.getClass();

        final List<VncKeyword> superclasses = new ArrayList<>();
        Class<?> superClass = type.getSuperclass();
        while(superClass != null) {
            superclasses.add(new VncKeyword(superClass.getName(), MetaUtil.typeMeta()));
            superClass = superClass.getSuperclass();
        }

        return new VncKeyword(
                    type.getName(),
                    MetaUtil.typeMeta(superclasses.toArray(new VncKeyword[0])));
    }

    public VncJavaObject castTo(final Class<?> clazz) {
        return VncJavaObject.from(delegate, clazz);
    }

    public VncVal getProperty(final VncString name) {
        return JavaInteropUtil.convertToVncVal(
                ThreadContext
                    .getInterceptor()
                    .onGetBeanProperty(
                            new Invoker(),
                            delegate,
                            name.getValue()));
    }

    public void setProperty(final VncString name, final VncVal value) {
        ThreadContext
            .getInterceptor()
            .onSetBeanProperty(
                    new Invoker(),
                    delegate,
                    name.getValue(),
                    value.convertToJavaObject());
    }

    @Override
    public Map<VncVal,VncVal> getJavaMap() {
        return convertBean().getJavaMap();
    }

    @Override
    public VncVal containsKey(final VncVal key) {
        return VncBoolean.of(getJavaMap().containsKey(key));
    }

    @Override
    public VncVal get(final VncVal key) {
    	if (key instanceof VncString) {
    		return getProperty((VncString)key);
    	}
    	else {
    	      throw new VncException(
    	    		  "VncJavaObject::get() requires a string or keyword as argument. "
    	    		  + "Got a " + Types.getType(key));
    	}
    }

    @Override
    public VncList keys() {
        return VncList.ofList(new ArrayList<>(getJavaMap().keySet()));
    }

    @Override
    public List<VncMapEntry> entries() {
        return Collections.unmodifiableList(
                    getJavaMap()
                        .entrySet()
                        .stream().map(e -> new VncMapEntry(e.getKey(), e.getValue()))
                        .collect(Collectors.toList()));
    }

    @Override
    public VncMap putAll(final VncMap map) {
        throw new VncException("VncJavaObject::assoc() is not supported");
    }

    @Override
    public VncMap assoc(final VncVal... mvs) {
        throw new VncException("VncJavaObject::assoc() is not supported");
    }

    @Override
    public VncMap assoc(final VncSequence mvs) {
        throw new VncException("VncJavaObject::assoc() is not supported");
    }

    @Override
    public VncMap dissoc(final VncVal... keys) {
        throw new VncException("VncJavaObject::dissoc() is not supported");
    }

    @Override
    public VncMap dissoc(final VncSequence keys) {
        throw new VncException("VncJavaObject::dissoc() is not supported");
    }

    @Override
    public VncList toVncList() {
        return VncList.empty();
    }

    @Override
    public VncVector toVncVector() {
        return VncVector.empty();
    }

    public VncMap toVncMap() {
        return new VncHashMap(getJavaMap());
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.JAVAOBJECT;
    }

    @Override
    public Object convertToJavaObject() {
        return delegate;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(final VncVal o) {
        if (delegate instanceof Comparable) {
            if (Types.isVncJavaObject(o)) {
                final Object other = ((VncJavaObject)o).getDelegate();
                if (other instanceof Comparable) {
                    return ((Comparable)delegate).compareTo((other));
                }
            }
        }
        else if (o == Constants.Nil) {
            return 1;
        }

        return super.compareTo(o);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        VncJavaObject other = (VncJavaObject) obj;
        if (delegate == null) {
            if (other.delegate != null)
                return false;
        } else if (!delegate.equals(other.delegate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return isArray()
                    ? ArrayFunctions.arrayToString(this)
                    : delegate.toString();
    }

    @Override
    public String toString(final boolean print_machine_readably) {
        return toString();
    }

    public boolean isArray() {
        return ReflectionTypes.isArrayType(delegate.getClass());
    }


    private VncHashMap convertBean() {
        final IInterceptor interceptor = ThreadContext.getInterceptor();
        final IInvoker invoker = new Invoker();

        final HashMap<VncVal,VncVal> map = new HashMap<>();

        ReflectionAccessor
            .getBeanGetterProperties(delegate)
            .forEach(property -> {
                try {
                    map.put(
                        new VncKeyword(property),
                        JavaInteropUtil.convertToVncVal(
                                interceptor.onGetBeanProperty(invoker, delegate, property)));
                }
                catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

        return new VncHashMap(map);
    }


    private static final long serialVersionUID = -1848883965231344442L;

    private final Object delegate;
    private final Class<?> delegateFormalType;
}
