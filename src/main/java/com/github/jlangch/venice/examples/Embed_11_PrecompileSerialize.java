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

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.PreCompiled;
import com.github.jlangch.venice.Venice;


public class Embed_11_PrecompileSerialize {
    
	public static void main(final String[] args) {
        final Venice venice = new Venice();

        PreCompiled precompiled = venice.precompile("example", "(+ 1 x)");
        
        final byte[] data = precompiled.serialize();
        
        // store the serialized expression on a DB for example
        
        // ...
        
        // reload the serialized expression to use it
        
        precompiled = PreCompiled.deserialize(data);

        System.out.println(venice.eval(precompiled, Parameters.of("x", 2)));
    }
	
}
