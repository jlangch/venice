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
package com.github.jlangch.venice.impl.functions;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.sandbox.RestrictedBlacklistedFunctions;
import com.github.jlangch.venice.impl.thread.ThreadContext;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.util.ArityExceptions;
import com.github.jlangch.venice.impl.util.SymbolMapBuilder;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.IInterceptor;
import com.github.jlangch.venice.javainterop.RejectAllInterceptor;
import com.github.jlangch.venice.javainterop.SandboxInterceptor;


public class SandboxFunctions {

    ///////////////////////////////////////////////////////////////////////////
    // Snadbox
    ///////////////////////////////////////////////////////////////////////////

    public static VncFunction sandboxed_Q =
        new VncFunction(
                "sandboxed?",
                VncFunction
                    .meta()
                    .arglists("(sandboxed?)")
                    .doc(
                        "Returns true if there is a sandbox other than `:AcceptAllInterceptor` " +
                        "otherwise false.")
                    .examples("(sandboxed?)")
                    .seeAlso("sandbox/type")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                return VncBoolean.of(ThreadContext.isSandboxed());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sandbox_type =
        new VncFunction(
                "sandbox/type",
                VncFunction
                    .meta()
                    .arglists("(sandbox/type)")
                    .doc(
                        "Returns the sandbox type. \n\n" +
                        "Venice sandbox types:\n\n" +
                        " * `:" + AcceptAllInterceptor.class.getSimpleName() + "` "
                                + "- accepts all (no restrictions)\n" +
                        " * `:" + RejectAllInterceptor.class.getSimpleName() + "` "
                                + "- safe sandbox, rejects access to all I/O functions, "
                                + "system properties, environment vars, extension modules, "
                                + "dynamic code loading, multi-threaded functions (futures, agents, ...), "
                                + "and Java calls\n" +
                        " * `:" + SandboxInterceptor.class.getSimpleName() + "` "
                                + "- customized sandbox")
                    .examples("(sandbox/type)")
                    .seeAlso("sandboxed?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 0);

                final IInterceptor interceptor = ThreadContext.getInterceptor();

                return interceptor == null
                        ? Constants.Nil
                        : new VncKeyword(interceptor.getClass().getSimpleName());
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };

    public static VncFunction sandbox_functions =
        new VncFunction(
                "sandbox/functions",
                VncFunction
                    .meta()
                    .arglists(
                    	"(sandbox/functions group)")
                    .doc(
                        "Lists the sandboxed functions defined by a sandbox function group.\n\n" +
                        "Groups:\n\n" +
                        " * :io \n" +
                        " * :print \n" +
                        " * :concurrency \n" +
                        " * :java-interop \n" +
                        " * :system \n" +
                        " * :special-forms \n" +
                        " * :unsafe")
                    .examples(
                        "(sandbox/functions :print)")
                    .seeAlso(
                    	"sandboxed?")
                    .build()
        ) {
            @Override
            public VncVal apply(final VncList args) {
                ArityExceptions.assertArity(this, args, 1, 2);

                final String group = Coerce.toVncKeyword(args.first()).getValue();

                return getGroup(group);
            }

            private static final long serialVersionUID = -1848883965231344442L;
        };


    private static VncList getGroup(final String group) {
        switch(group) {
	    	case "io":             return toVncList(RestrictedBlacklistedFunctions.getIoFunctions());
	    	case "print":          return toVncList(RestrictedBlacklistedFunctions.getPrintFunctions());
	    	case "concurrency":    return toVncList(RestrictedBlacklistedFunctions.getConcurrencyFunctions());
        	case "java-interop":   return toVncList(RestrictedBlacklistedFunctions.getJavaInteropFunctions());
        	case "system":         return toVncList(RestrictedBlacklistedFunctions.getSystemFunctions());
        	case "special-forms":  return toVncList(RestrictedBlacklistedFunctions.getSpecialForms());
        	case "unsafe":         return toVncList(RestrictedBlacklistedFunctions.getAllFunctions());
        	default:
        		throw new VncException(
        				"Unsupported group! Choose one of " +
        				"{:io, :java-interop, :system, :special-forms, :unsafe}");

        }
    }

    private static VncList toVncList(Set<String> set) {
    	return VncList.ofColl(
			    	set.stream()
			    	   .sorted()
			    	   .map(s -> new VncString(s))
			    	   .collect(Collectors.toList()));
    }


    ///////////////////////////////////////////////////////////////////////////
    // types_ns is namespace of type functions
    ///////////////////////////////////////////////////////////////////////////

    public static final Map<VncVal, VncVal> ns =
            new SymbolMapBuilder()

                    .add(sandboxed_Q)
                    .add(sandbox_type)
                    .add(sandbox_functions)

                    .toMap();

}
