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
import java.util.Map;

import com.github.jlangch.venice.impl.javainterop.JavaInteropFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_DefFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_ImportFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_MethodFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_NamespaceFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_OtherFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_TryCatchFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_TypeFunctions;
import com.github.jlangch.venice.impl.specialforms.SpecialForms_VarFunctions;
import com.github.jlangch.venice.impl.types.VncVal;


public class Functions {

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

        functions.putAll(CoreFunctions.ns);
        functions.putAll(ExceptionFunctions.ns);
        functions.putAll(BytebufFunctions.ns);
        functions.putAll(TransducerFunctions.ns);
        functions.putAll(ModuleFunctions.ns);
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
        functions.putAll(ScheduleFunctions.ns);
        functions.putAll(ConcurrencyFunctions.ns);
        functions.putAll(JsonFunctions.ns);
        functions.putAll(PdfFunctions.ns);
        functions.putAll(JavaInteropFunctions.ns);
        functions.putAll(CidrFunctions.ns);
        functions.putAll(CsvFunctions.ns);
        functions.putAll(DagFunctions.ns);
    }

}
