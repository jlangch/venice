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
package com.github.jlangch.venice.util;


public class OS {

    public static OsType type() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows")) {
            return OsType.Windows;
        }
        else if (osName.startsWith("mac os x")) {
            return OsType.MacOsx;
        }
        else if (osName.startsWith("linux")) {
            return OsType.Linux;
        }
        else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))  {
            return OsType.Unix;
        }
        else {
            return OsType.Unknown;
        }
    }

    public static boolean isMacOsx() {
        return OsType.MacOsx == type();
    }

    public static boolean isLinux() {
        return OsType.Linux == type();
    }

    public static boolean isWindows() {
        return OsType.Windows == type();
    }


    public static enum OsType { MacOsx, Unix, Linux, Windows, Unknown };
}
