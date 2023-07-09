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
package com.github.jlangch.venice.examples;

import org.junit.jupiter.api.Test;


public class ExamplesTest {

    @Test
    public void test_Embed_01_Simple() {
        Embed_01_Simple.run();
    }

    @Test
    public void test_Embed_02_PassingParameters() {
        Embed_02_PassingParameters.run();
    }

    @Test
    public void test_Embed_03_StdOutRedirection() {
        Embed_03_StdOutRedirection.main(new String[0]);
    }

    @Test
    public void test_Embed_04_Precompile() {
        Embed_04_Precompile.run();
    }

    @Test
    public void test_Embed_05_Exceptions() {
    	Embed_05_Exceptions.main(new String[0]);
    }

    @Test
    public void test_Embed_08_JavaInterop() {
        Embed_08_JavaInterop.run();
    }

    @Test
    public void test_Embed_09_StrictSandbox() {
        Embed_09_StrictSandbox.run();
    }

    @Test
    public void test_Embed_10_CustomMinimalSandbox() {
        Embed_10_CustomMinimalSandbox.main(new String[0]);
    }

    @Test
    public void test_Embed_11_CustomSandbox() {
        Embed_11_CustomSandbox.run();
    }

    @Test
    public void test_Embed_12_ServiceRegistry() {
    	Embed_12_ServiceRegistry.run();
    }

    @Test
    public void test_Embed_13_DynServiceRegistry() {
    	Embed_13_DynServiceRegistry.run();
    }

}
