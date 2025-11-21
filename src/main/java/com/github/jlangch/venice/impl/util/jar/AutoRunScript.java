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
package com.github.jlangch.venice.impl.util.jar;

import static com.github.jlangch.venice.impl.util.CollectionUtil.first;
import static com.github.jlangch.venice.impl.util.StringUtil.splitIntoLines;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToEmpty;
import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import java.nio.charset.StandardCharsets;

import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class AutoRunScript {

    public static String loadAutoRunScript() {
        final byte[] script = new ClassPathResource(AutoRunScriptJarRewriter.AUTORUN_SCRIPT_PATH)
                                       .getResourceAsBinary();
        if (script == null) {
            throw new RuntimeException("Failed to load embedded auto run script!");
        }

        return deobfuscate(script);
    }

    public static String loadAutoRunScriptName() {
        try {
            final String data = new ClassPathResource(AutoRunScriptJarRewriter.AUTORUN_META_PATH)
                                        .getResourceAsString("UTF-8");

            final String line = trimToEmpty(first(splitIntoLines(data)));

            final String name = trimToNull(line.split("=")[1]);

            return name == null ? "autorun" : name;
        }
        catch(Exception ex) {
            return "autorun";
        }
    }

    public static byte[] obfuscate(final String script) {
        final byte[] data = script.getBytes(StandardCharsets.UTF_8);
        return obfuscate ? xor(data) : data;
    }

    public static String deobfuscate(final byte[] script) {
        final byte[] data = obfuscate ? xor(script) : script;
        return new String(data, StandardCharsets.UTF_8);
    }


    private static byte[] xor(final byte[] data) {
        // very simple obfuscation, no bells and whistles
        for(int ii=0; ii<data.length; ii++) {
            data[ii] = (byte)(data[ii] ^ (byte)0x55);
        }
        return data;
    }


    private static boolean obfuscate = true;
}
