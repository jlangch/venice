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
package com.github.jlangch.venice.demo.mbean;

/**
 * A JMX standard MBean
 *
 * https://docs.oracle.com/javase/tutorial/jmx/mbeans/standard.html
 */
public class Hello implements HelloMBean {

    public Hello() {
    }

    @Override
    public void sayHello() {
        System.out.println("Hello, world!");
    }

    @Override
    public int add(int x, int y) {
        return x + y;
    }

    @Override
    public int getMaxCount() {
        return maxCount;
    }

    @Override
    public void setMaxCount(int c) {
        maxCount = c;
    }

    int maxCount = 42;
}