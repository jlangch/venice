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
package com.github.jlangch.venice.impl.util;

import java.nio.charset.Charset;
import java.util.Random;


public class Babelizer {

     public static String babelize(final String arg) {
        if (arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException("The 'arg' string must not be null or empty!");
        }

        final StringBuilder sb = new StringBuilder();

        final Random r = new Random(System.currentTimeMillis());
        final byte[] b = arg.getBytes(Charset.forName("UTF-8"));
        final int c = b.length;

        sb.append("(new Object() {");
        sb.append("int t;");
        sb.append("public String toString() {");
        sb.append("byte[] buf = new byte[");
        sb.append(c);
        sb.append("];");

        for (int i = 0; i < c; ++i) {
            int t = r.nextInt();
            int f = r.nextInt(24) + 1;

            t = (t & ~(0xff << f)) | (b[i] << f);

            sb.append("t = ");
            sb.append(t);
            sb.append(";");
            sb.append("buf[");
            sb.append(i);
            sb.append("] = (byte) (t >>> ");
            sb.append(f);
            sb.append(");");
        }

        sb.append("return new String(buf);");
        sb.append("}}.toString())");

        return sb.toString();
    }
}
