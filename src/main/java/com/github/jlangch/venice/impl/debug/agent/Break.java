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
package com.github.jlangch.venice.impl.debug.agent;

import static com.github.jlangch.venice.impl.util.StringUtil.padRight;

import java.util.UUID;

import com.github.jlangch.venice.impl.debug.breakpoint.BreakpointFnRef;
import com.github.jlangch.venice.impl.debug.breakpoint.FunctionScope;
import com.github.jlangch.venice.impl.debug.util.SpecialFormVirtualFunction;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.SourcePos;
import com.github.jlangch.venice.impl.util.callstack.CallStack;


/**
 * Represents a break with its context information
 */
public class Break {

    public Break(
            final BreakpointFnRef breakpoint,
            final VncFunction fn,
            final VncList args,
            final Env env,
            final CallStack callStack,
            final FunctionScope scope,
            final String threadName
    ) {
        this(breakpoint, fn, args, null, null, env, callStack, scope, threadName);
    }

    public Break(
            final BreakpointFnRef breakpoint,
            final VncFunction fn,
            final VncList args,
            final VncVal retVal,
            final Exception ex,
            final Env env,
            final CallStack callStack,
            final FunctionScope scope,
            final String threadName
    ) {
        this.breakpoint = breakpoint;
        this.fn = fn;
        this.args = args;
        this.retVal = retVal;
        this.ex = ex;
        this.env = env;
        this.callStack = callStack;
        this.scope = scope;
        this.threadName = threadName;
    }


    public BreakpointFnRef getBreakpoint() {
        return breakpoint;
    }

    public VncFunction getFn() {
        return fn;
    }

    public VncList getArgs() {
        return args;
    }

    public VncVal getRetVal() {
        return retVal;
    }

    public Exception getException() {
        return ex;
    }

    public Env getEnv() {
        return env;
    }

    public CallStack getCallStack() {
        return callStack;
    }

    public FunctionScope getBreakpointScope() {
        return scope;
    }

    public boolean isInScope(final FunctionScope... scopes) {
        return CollectionUtil.toList(scopes).contains(scope);
    }

    public boolean isBreakInSpecialForm() {
        return fn instanceof SpecialFormVirtualFunction;
    }

    public boolean isBreakInNativeFn() {
        return fn.isNative();
    }

    public String getThreadName() {
        return threadName;
    }

    public String getBreakFnInfo(final boolean pad) {
        final int padLen = pad ? FORMAT_PAD_LEN : 0;

        if (isBreakInSpecialForm()) {
            return String.format(
                        "%s %s",
                        padRight("Special Form:", padLen),
                        fn.getQualifiedName());
        }
        else if (fn.isMacro()) {
            final SourcePos pos = SourcePos.fromVal(fn);

            return String.format(
                        "%s %s defined in %s at line %d",
                        padRight("Macro:", padLen),
                        fn.getQualifiedName(),
                        pos.getFile(),
                        pos.getLine());
        }
        else if (fn.isNative()) {
            return String.format(
                        "%s %s (native, no source line info)",
                        padRight("Function:", padLen),
                        fn.getQualifiedName());
        }
        else {
            final SourcePos pos = SourcePos.fromVal(fn);

            return String.format(
                        "%s %s defined in %s at line %d",
                        padRight("Function:", padLen),
                        fn.getQualifiedName(),
                        pos.getFile(),
                        pos.getLine());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format(
                        "%s %s",
                        padRight("Breakpoint:", FORMAT_PAD_LEN),
                        breakpoint.toString()));

        sb.append("\n");
        sb.append(getBreakFnInfo(true));

        sb.append("\n");
        sb.append(String.format(
                        "%s %s",
                        padRight("Scope:", FORMAT_PAD_LEN),
                        scope));

        sb.append("\n");
        sb.append(String.format(
                        "%s %s",
                        padRight("Thread:", FORMAT_PAD_LEN),
                        threadName));

        sb.append("\n");
        sb.append(String.format(
                        "%s %d frames",
                        padRight("Callstack:", FORMAT_PAD_LEN),
                        getCallStack().size()));

        return sb.toString();
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Break other = (Break) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }


    public static int FORMAT_PAD_LEN = 14;

    private final String id = UUID.randomUUID().toString();
    private final BreakpointFnRef breakpoint;
    private final VncFunction fn;
    private final VncList args;
    private final VncVal retVal;
    private final Exception ex;
    private final Env env;
    private final CallStack callStack;
    private final FunctionScope scope;
    private final String threadName;
}
