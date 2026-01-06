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
package com.github.jlangch.venice.util.password;

import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;


/**
 * Creates salts for encoding passwords.
 *
 * @author juerg
 */
public class SaltSource {

    public SaltSource() {
    }

    public String createSalt() {
        synchronized(random) {
            final int salt = random.nextInt(10000000);
            return StringUtils.leftPad(String.valueOf(salt), 7, '0');
        }
    }


    private static SecureRandom random = new SecureRandom();
}
