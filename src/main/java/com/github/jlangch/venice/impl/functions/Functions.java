/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.javainterop.JavaInteropFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_DefFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_ImportFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_LoadCodeMacros;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_MethodFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_NamespaceFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_OtherFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_TryCatchFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_TypeFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_VarFunctions;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncSpecialForm;
import com.github.jlangch.venice.impl.types.VncVal;


public class Functions {

    public static void main(String[] args){
        System.out.println("// SpecialForms:");
        System.out.print(String.join(",\n", getSpecialForms()));
        System.out.println(",\n");

        System.out.println("// Module functions:");
        System.out.print(String.join(",\n", getVncFunctions(LoadPathFunctions.ns)));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// I/O:");
        System.out.print(String.join(",\n", getIoFunctions()));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// Concurrency:");
        System.out.print(String.join(",\n", getVncFunctions(ConcurrencyFunctions.ns)));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// Java Interop");
        System.out.print(String.join(",\n", getVncFunctions(JavaInteropFunctions.ns)));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// Scheduler");
        System.out.print(String.join(",\n", getVncFunctions(ScheduleFunctions.ns)));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// System");
        System.out.print(String.join(",\n", getVncFunctions(SystemFunctions.ns)));
        System.out.println(",\n");

        System.out.println();
        System.out.println("// Shell");
        System.out.print(String.join(",\n", getVncFunctions(ShellFunctions.ns)));
        System.out.println("\n");
    }

    public static List<String> getSpecialForms() {
        return functions
                .values()
                .stream()
                .filter(f -> f instanceof VncSpecialForm)
                .map(f -> "\"" + ((VncSpecialForm)f).getName() + "\"")
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getIoFunctions() {
        return functions
                .values()
                .stream()
                .filter(f -> (f instanceof VncFunction)
                                && "io".equals(((VncFunction)f).getNamespace()))
                .map(f -> "\"" + ((VncFunction)f).getQualifiedName() + "\"")
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getVncFunctions(final Map<VncVal, VncVal> ns) {
        return ns.values()
                 .stream()
                 .filter(f -> (f instanceof VncFunction))
                 .map(f -> "\"" + ((VncFunction)f).getQualifiedName() + "\"")
                 .sorted()
                 .collect(Collectors.toList());
    }

    public static final Map<VncVal,VncVal> functions = new HashMap<>();

    static {
        functions.putAll(SpecialForms_DefFunctions.ns);
        functions.putAll(SpecialForms_ImportFunctions.ns);
        functions.putAll(SpecialForms_TypeFunctions.ns);
        functions.putAll(SpecialForms_MethodFunctions.ns);
        functions.putAll(SpecialForms_NamespaceFunctions.ns);
        functions.putAll(SpecialForms_TryCatchFunctions.ns);
        functions.putAll(SpecialForms_VarFunctions.ns);
        functions.putAll(SpecialForms_OtherFunctions.ns);
        functions.putAll(SpecialForms_LoadCodeMacros.ns);

        functions.putAll(CoreFunctions.ns);
        functions.putAll(CoreConcurrencyFunctions.ns);
        functions.putAll(CoreSystemFunctions.ns);
        functions.putAll(ExceptionFunctions.ns);
        functions.putAll(BytebufFunctions.ns);
        functions.putAll(TransducerFunctions.ns);
        functions.putAll(LoadPathFunctions.ns);
        functions.putAll(StringFunctions.ns);
        functions.putAll(RegexFunctions.ns);
        functions.putAll(ArrayFunctions.ns);
        functions.putAll(MathFunctions.ns);
        functions.putAll(IOFunctions.ns);
        functions.putAll(IOFunctionsSpitSlurp.ns);
        functions.putAll(IOFunctionsStreams.ns);
        functions.putAll(ZipFunctions.ns);
        functions.putAll(TimeFunctions.ns);
        functions.putAll(ShellFunctions.ns);
        functions.putAll(SystemFunctions.ns);
        functions.putAll(SandboxFunctions.ns);
        functions.putAll(ScheduleFunctions.ns);
        functions.putAll(ConcurrencyFunctions.ns);
        functions.putAll(JsonFunctions.ns);
        functions.putAll(PdfFunctions.ns);
        functions.putAll(JavaInteropFunctions.ns);
        functions.putAll(InetFunctions.ns);
        functions.putAll(CidrFunctions.ns);
        functions.putAll(CsvFunctions.ns);
        functions.putAll(DagFunctions.ns);
    }

}
