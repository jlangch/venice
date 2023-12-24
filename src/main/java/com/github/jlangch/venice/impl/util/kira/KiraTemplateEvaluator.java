/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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
package com.github.jlangch.venice.impl.util.kira;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class KiraTemplateEvaluator {

	public KiraTemplateEvaluator() {

	}

    public String evaluateKiraTemplate(
            final String template,
            final Map<String,Object> data
    ) {
        final String script =
                "(do                                           \n" +
                "   (load-module :kira)                        \n" +
                "   (kira/eval template [\"${\" \"}$\"] data))   ";

        return (String)new Venice().eval(
                            script,
                            Parameters.of("template", template, "data", data));
    }

    public <T> T runAsync(final Callable<T> callable) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(callable)
                           .get();
        }
        finally {
            executor.shutdownNow();
        }
    }

}
