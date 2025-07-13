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
package com.github.jlangch.venice;

import java.util.List;

import com.github.jlangch.venice.impl.util.CollectionUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public abstract class LicenseMgr {


    public static String loadVeniceLicenseText() {
        return new ClassPathResource("META-INF/" + LICENSE_VENICE_TEXT).getResourceAsString("UTF-8");
    }

    public static String loadVeniceLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_VENICE).getResourceAsString("UTF-8");
    }

    public static String loadAudiowideLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_AUDIOWIDE).getResourceAsString("UTF-8");
    }

    public static String loadJLine3License() {
        return new ClassPathResource("META-INF/" + LICENSE_JLINE3).getResourceAsString("UTF-8");
    }

    public static String loadNanojsonLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_NANOJSON).getResourceAsString("UTF-8");
    }

    public static String loadVavrLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_VAVR).getResourceAsString("UTF-8");
    }

    public static String loadZip4Jicense() {
        return new ClassPathResource("META-INF/" + LICENSE_ZIP4J).getResourceAsString("UTF-8");
    }

    public static String loadOflLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_OFL).getResourceAsString("UTF-8");
    }

    public static String loadOpenSansLicense() {
        return new ClassPathResource("META-INF/" + LICENSE_OPENSANS).getResourceAsString("UTF-8");
    }

    public static String loadAll() {
        final List<String> licenses = CollectionUtil.toList(
                                            header("Venice License"),
                                            loadVeniceLicenseText(),
                                            header("Venice License"),
                                            loadJLine3License(),
                                            header("JLine3 License"),
                                            loadNanojsonLicense(),
                                            header("NanJson License"),
                                            loadVavrLicense(),
                                            header("Vavr License"),
                                            loadZip4Jicense(),
                                            header("SIL Open Font License"),
                                            loadOflLicense(),
                                            header("Audowide Font License"),
                                            loadAudiowideLicense(),
                                            header("OpenSans Font License"),
                                            loadOpenSansLicense());

        return String.join("\n\n\n\n", licenses);
    }


    private static String header(final String header) {
        return StringUtil.repeat('*', 80) + "\n" +
               "* " + header + StringUtil.repeat(' ', 80 - 3 -header.length()) + "*\n" +
               StringUtil.repeat('*', 80) + "\n" +
               "\n\n";
    }


    private final static String LICENSE_VENICE_TEXT = "license.txt";
    private final static String LICENSE_VENICE = "LICENSE-Venice.txt";
    private final static String LICENSE_AUDIOWIDE = "LICENSE-Audiowide.txt";
    private final static String LICENSE_JLINE3 = "LICENSE-JLine3.txt";
    private final static String LICENSE_NANOJSON = "LICENSE-nanojson.txt";
    private final static String LICENSE_VAVR = "LICENSE-Vavr.txt";
    private final static String LICENSE_ZIP4J = "LICENSE-Zip4J.txt";
    private final static String LICENSE_OFL = "LICENSE-OFL.txt";
    private final static String LICENSE_OPENSANS = "LICENSE-OpenSans.txt";
}
