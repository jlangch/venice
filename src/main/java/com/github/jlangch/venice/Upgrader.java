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
package com.github.jlangch.venice;

import com.github.jlangch.venice.impl.repl.ReplUpgrade;


/**
 * Venice REPL upgrader
 */
public class Upgrader {

    public static void main(final String[] args) {
        try {
            System.out.println("REPL upgrading...");
            final String newVersion = ReplUpgrade.upgrade();
            System.out.println("REPL upgraded to version " + newVersion + "!");
            System.out.println("Starting upgraded REPL...\n");
            System.exit(0);
        }
        catch(Exception ex) {
            System.out.println("REPL upgrade failed! Reason: " + ex.getMessage());
            System.exit(1);
        }
    }
}
