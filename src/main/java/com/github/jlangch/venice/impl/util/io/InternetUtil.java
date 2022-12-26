/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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
package com.github.jlangch.venice.impl.util.io;

import java.net.URL;
import java.net.URLConnection;


public class InternetUtil {

    public static boolean isInternetAvailable(final String sURL) {
        try {
            final URL url = new URL(sURL);
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            connection.connect();
            connection.getInputStream().close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isInternetAvailable() {
        return isInternetAvailable("http://www.google.com");
    }

}
