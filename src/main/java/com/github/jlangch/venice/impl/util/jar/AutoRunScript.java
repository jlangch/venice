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

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;
import com.github.jlangch.venice.util.crypt.Encryptor_AES256_GCM;


public class AutoRunScript {

    public static String loadAutoRunScript() {
        final byte[] script = new ClassPathResource(AutoRunScriptJarRewriter.AUTORUN_SCRIPT_PATH)
                                       .getResourceAsBinary();
        if (script == null) {
            throw new VncException("Failed to load embedded auto run script!");
        }

        return decrypt(script);
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

    public static byte[] encrypt(final String script) {
        try {
            final byte[] data = script.getBytes(StandardCharsets.UTF_8);

            return obfuscate
                    ? Encryptor_AES256_GCM
                        .create(fus())
                        .encrypt(data)
                    : data;
        }
        catch(Exception ex) {
           return null;
        }
    }

    public static String decrypt(final byte[] script) {
        try {
            final byte[] data = obfuscate
                                 ? Encryptor_AES256_GCM
                                     .create(fus())
                                     .decrypt(script)
                                 : script;

             return new String(data, StandardCharsets.UTF_8);
        }
        catch(Exception ex) {
            return null;
        }
    }

    private static String fus() {
        return (new Object() {int t;@Override public String toString() {byte[] buf = new byte[28];t = -1257081704;buf[0] = (byte) (t >>> 2);t = 1145809682;buf[1] = (byte) (t >>> 5);t = 1181957387;buf[2] = (byte) (t >>> 11);t = -948017553;buf[3] = (byte) (t >>> 20);t = 1569066953;buf[4] = (byte) (t >>> 18);t = 1670201803;buf[5] = (byte) (t >>> 2);t = -1610400739;buf[6] = (byte) (t >>> 12);t = -1495710215;buf[7] = (byte) (t >>> 20);t = 653685781;buf[8] = (byte) (t >>> 12);t = 1815857001;buf[9] = (byte) (t >>> 11);t = -938712664;buf[10] = (byte) (t >>> 2);t = 1818510320;buf[11] = (byte) (t >>> 16);t = 2011648104;buf[12] = (byte) (t >>> 24);t = 1616479451;buf[13] = (byte) (t >>> 2);t = -1304186341;buf[14] = (byte) (t >>> 7);t = 1705638520;buf[15] = (byte) (t >>> 18);t = -1796473847;buf[16] = (byte) (t >>> 17);t = -202158379;buf[17] = (byte) (t >>> 6);t = -961168845;buf[18] = (byte) (t >>> 15);t = -981195427;buf[19] = (byte) (t >>> 2);t = -1312107183;buf[20] = (byte) (t >>> 18);t = 252220048;buf[21] = (byte) (t >>> 13);t = -1642819155;buf[22] = (byte) (t >>> 22);t = 1482471441;buf[23] = (byte) (t >>> 22);t = 1785946158;buf[24] = (byte) (t >>> 19);t = 1262198107;buf[25] = (byte) (t >>> 15);t = 1242857266;buf[26] = (byte) (t >>> 21);t = -1414553383;buf[27] = (byte) (t >>> 2);return new String(buf);}}.toString());
    }


    private static boolean obfuscate = true;
}
