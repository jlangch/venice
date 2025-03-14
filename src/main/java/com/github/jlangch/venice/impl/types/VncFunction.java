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
package com.github.jlangch.venice.impl.types;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jlangch.venice.impl.FunctionMetaBuilder;
import com.github.jlangch.venice.impl.continuation.VncContinuationFunction;
import com.github.jlangch.venice.impl.debug.util.SpecialFormVirtualFunction;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncVector;
import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.callstack.CallFrame;
import com.github.jlangch.venice.javainterop.IInterceptor;


public abstract class VncFunction
    extends VncVal
    implements IVncFunction,
               INamespaceAware {

    public VncFunction(final String name) {
        this(name, null, false, null, Constants.Nil);
    }

    public VncFunction(final String name, final VncVal meta) {
        this(name, null, false, null, meta);
    }

    public VncFunction(final String name, final VncVector params, final VncVal meta) {
        this(name, params, false, null, meta);
    }

    public VncFunction(final String name, final VncVector params) {
        this(name, params, false, null, Constants.Nil);
    }

    public VncFunction(
            final String name,
            final VncVector params,
            final boolean macro,
            final VncVector preConditions,
            final VncVal meta
    ) {
        super(Constants.Nil);

        final QualifiedName qn = QualifiedName.parse(name);

        this.namespace = qn.getNamespace();
        this.simpleName = qn.getSimpleName();
        this.qualifiedName = qn.getQualifiedName();

        this.params = params == null ? VncVector.empty() : params;

        int fixedArgs = 0;
        boolean variadic = false;
        VncVal variadicName = null;
        if (params != null) {
            for(VncVal p : params) {
                if (isElisionSymbol(p)) {
                    variadic = true;
                    variadicName = params.last();
                    break;
                }
                fixedArgs++;
            }
        }

        this.fixedArgsCount = fixedArgs;
        this.variadicArgs = variadic;
        this.variadicArgName = variadicName != null && Types.isVncSymbol(variadicName)
                                 ? ((VncSymbol)variadicName).getSimpleName()
                                 : null;

        this.anonymous = isAnonymousFuncName(simpleName);
        this.macro = macro;

        this.preConditions = preConditions;
        this.fnMeta.set(MetaUtil.setNamespace(meta, namespace));
        this.fnPrivate = MetaUtil.isPrivate(meta);
    }

    public static VncFunction of(final IVncFunction fn) {
        return new VncFunction(createAnonymousFuncName()) {
            @Override
            public VncVal apply(final VncList args) {
                return fn.apply(args);
            }
            private static final long serialVersionUID = 1L;
        };
    }

    public static VncFunction of(final Supplier<VncVal> fn) {
        return new VncFunction(createAnonymousFuncName()) {
            @Override
            public VncVal apply(final VncList args) {
                return fn.get();
            }
            private static final long serialVersionUID = 1L;
        };
    }

    public static VncFunction of(final Function<VncList,VncVal> fn) {
        return new VncFunction(createAnonymousFuncName()) {
            @Override
            public VncVal apply(final VncList args) {
                return fn.apply(args);
            }
            private static final long serialVersionUID = 1L;
        };
    }

    @Override
    public VncFunction withMeta(final VncVal meta) {
        this.fnMeta.set(meta);
        this.fnPrivate = MetaUtil.isPrivate(meta);
        return this;
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                    isMacro() ? TYPE_MACRO : TYPE_FUNCTION,
                    MetaUtil.typeMeta(
                        new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public abstract VncVal apply(final VncList args);

    @Override
    public VncVal applyOf(final VncVal... mvs) {
        return apply(VncList.of(mvs));
    }

    public VncFunction getFunctionForArgs(final VncList args) {
        return getFunctionForArity(args.size());
    }

    public VncFunction getFunctionForArity(final int arity) {
        return this;
    }

    @Override
    public boolean isNative() {
        return true; // implemented natively in Java
    }

    public boolean isRedefinable() {
        return true;
    }

    public VncVector getParams() {
        return params;
    }

    @Override
    public boolean isMacro() {
        return macro;
    }

    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean hasNamespace() {
        return namespace != null;
    }

    @Override
    public VncList getArgLists() {
        return (VncList)getMetaVal(MetaUtil.ARGLIST, VncList.empty());
    }

    public VncVal getDoc() {
        return getMetaVal(MetaUtil.DOC);
    }

    public VncVal getAdded() {
        return getMetaVal(MetaUtil.ADDED);
    }

    public VncList getExamples() {
        return (VncList)getMetaVal(MetaUtil.EXAMPLES, VncList.empty());
    }

    public VncList getSeeAlso() {
        return (VncList)getMetaVal(MetaUtil.SEE_ALSO, VncList.empty());
    }

    public int getFixedArgsCount() {
        return fixedArgsCount;
    }

    public boolean hasVariadicArgs() {
        return variadicArgs;
    }

    public String getVariadicArgName() {
        return variadicArgName;
    }

    public VncVal getBody() {
        return Constants.Nil;
    }

    public VncVector getPreConditions() {
        return preConditions;
    }

    public boolean hasPreConditions() {
        return preConditions != null && !preConditions.isEmpty();
    }

    public VncMap about() {
        final CallFrame cf = new CallFrame(this);

        return VncOrderedMap.of(
                new VncKeyword("name"),       new VncString(getQualifiedName()),
                new VncKeyword("ns"),         new VncString(namespace == null ? "core" : namespace),
                new VncKeyword("type"),       new VncKeyword(isMacro() ? "macro" : "function"),
                new VncKeyword("visibility"), new VncKeyword(isPrivate() ? "private" : "public"),
                new VncKeyword("native"),     VncBoolean.of(isNative()),
                new VncKeyword("class"),      new VncKeyword(getFnClassType()),
                new VncKeyword("source"),     isNative()
                                                ? new VncOrderedMap()
                                                : VncOrderedMap.of(
                                                    new VncKeyword("file"),   new VncString(cf.getFile()),
                                                    new VncKeyword("line"),   new VncLong(cf.getLine()),
                                                    new VncKeyword("column"), new VncLong(cf.getCol())));
    }

    @Override
    public VncVal getMeta() {
        return fnMeta.get();
    }

    @Override
    public boolean isPrivate() {
        return fnPrivate;
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.FUNCTION;
    }

    @Override
    public Object convertToJavaObject() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
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
        VncFunction other = (VncFunction) obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null)
                return false;
        } else if (!qualifiedName.equals(other.qualifiedName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

    @Override
    public IInterceptor sandboxFunctionCallValidation() {
        return ThreadContext.getInterceptor()
                            .validateVeniceFunction(qualifiedName);
    }


    private String getFnClassType() {
        if (this instanceof VncMultiArityFunction) {
            return ":VncMultiArityFunction";
        }
        else if (this instanceof VncProtocolFunction) {
            return ":VncProtocolFunction";
        }
        else if (this instanceof VncMultiFunction) {
            return ":VncMultiFunction";
        }
        else if (this instanceof VncContinuationFunction) {
            return ":VncContinuationFunction";
        }
        else if (this instanceof SpecialFormVirtualFunction) {
            return ":SpecialFormVirtualFunction";
        }
        else if (this instanceof VncFunction) {
            return ":VncFunction";
        }
        else {
            return ":unknown";
        }
    }


    public static String createAnonymousFuncName() {
        return createAnonymousFuncName(null);
    }

    public static String createAnonymousFuncName(final String name) {
        return StringUtil.isEmpty(name)
                ? "anonymous-" + UUID.randomUUID().toString()
                : "anonymous-" + name + "-" + UUID.randomUUID().toString();
    }

    public static boolean isAnonymousFuncName(final String name) {
        return name == null || name.startsWith("anonymous-");
    }

    public static boolean isElisionSymbol(final VncVal val) {
        return (val instanceof VncSymbol) && ((VncSymbol)val).getName().equals("&");
    }

    public static boolean isElisionSymbol(final String val) {
        return val.equals("&");
    }


    public static FunctionMetaBuilder meta() {
        return new FunctionMetaBuilder();
    }


    public static VncVal applyWithMeter(
            final IVncFunction fn,
            final VncList args,
            final MeterRegistry meterRegistry
    ) {
        if (meterRegistry.enabled && fn.isNative()) {
            // Non native functions are profiled by the VeniceInterpreter while executing
            // (interpreting) the function. Do not profile them twice!
            final long nanos = System.nanoTime();

            final VncVal result = fn.apply(args);

            meterRegistry.record(((VncFunction)fn).getQualifiedName(), System.nanoTime() - nanos);

            return result;
        }
        else {
            return fn.apply(args);
        }
    }





    public static final String TYPE_FUNCTION = ":core/function";
    public static final String TYPE_MACRO = ":core/macro";

    private static final long serialVersionUID = -1848883965231344442L;

    private final String namespace;
    private final String simpleName;
    private final String qualifiedName;

    private final VncVector params;
    private final int fixedArgsCount;
    private final boolean variadicArgs;
    private final String variadicArgName;

    private final VncVector preConditions;

    private final boolean anonymous;
    private final boolean macro;

    // Functions handle its meta data locally (functions cannot be copied)
    private final AtomicReference<VncVal> fnMeta = new AtomicReference<>(Constants.Nil);
    private volatile boolean fnPrivate;
}
