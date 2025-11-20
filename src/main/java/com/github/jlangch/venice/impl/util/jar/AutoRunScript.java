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
import com.github.jlangch.venice.util.crypt.Encryptor_AES256_GCM;


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
        try {
            final byte[] data = script.getBytes(StandardCharsets.UTF_8);

            // just obfuscate the script not encrypt!
            return obfuscate
                    ? Encryptor_AES256_GCM
                        .create("xxxx")
                        .encrypt(data)
                    : data;
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to process embedded auto run script!");
        }
    }

    public static String deobfuscate(final byte[] script) {
        try {
            // just deobfuscate the script not decrypt!
            final byte[] data = obfuscate
                                 ? Encryptor_AES256_GCM
                                     .create("xxxx")
                                     .decrypt(script)
                                 : script;

             return new String(data, StandardCharsets.UTF_8);
        }
        catch(Exception ex) {
            throw new RuntimeException("Failed to load embedded auto run script!");
        }
    }


    private static boolean obfuscate = true;
}
