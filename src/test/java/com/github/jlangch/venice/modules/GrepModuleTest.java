/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.modules;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.io.ClassPathResource;


public class GrepModuleTest {

    @Test
    public void test_grep() {
        final Venice venice = new Venice();

        try {
            venice.eval("grep-test.venice", loadScript("grep-test.venice"));
        }
        catch(VncException ex) {
            System.err.println(ex.getMessage());
            System.err.println(ex.getCallStackAsString("  "));
            throw new RuntimeException(ex);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    private String loadScript(final String name) {
        final String scriptPath = getThisBasePath() + name;
        return new ClassPathResource(scriptPath).getResourceAsString("UTF-8");
    }

    private String getThisBasePath() {
        return this.getClass().getPackage().getName().replace('.', '/') + "/";
    }

}
