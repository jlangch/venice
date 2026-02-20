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
package com.github.jlangch.venice.impl.repl.remote;

import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.collections.VncMap;


public class FormResult {

    private FormResult(
            final String form,
            final String result,
            final String ex,
            final String out,
            final String err,
            final long elapsedMillis
    ) {
        this.form = form;
        this.result = result;
        this.ex = ex;
        this.out = out;
        this.err = err;
        this.elapsedMillis = elapsedMillis;
    }


    public static FormResult of(final VncMap result) {
        return new FormResult(
                    ((VncString)result.get(new VncString("form"))).getValue(),
                    ((VncString)result.get(new VncString("return"))).getValue(),
                    ((VncString)result.get(new VncString("ex"))).getValue(),
                    ((VncString)result.get(new VncString("out"))).getValue(),
                    ((VncString)result.get(new VncString("err"))).getValue(),
                    ((VncLong)result.get(new VncString("ms"))).getValue());
    }


    public String getForm() {
        return form;
    }

    public String getResult() {
        return result;
    }

    public String getEx() {
        return ex;
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }


    private final String form;
    private final String result;
    private final String ex;
    private final String out;
    private final String err;
    private final long elapsedMillis;
}
