/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.impl;

import static com.github.jlangch.venice.impl.FunctionsUtil.assertArity;

import java.util.Map;

import com.github.jlangch.venice.impl.types.Coerce;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class TimeFunctions {

	///////////////////////////////////////////////////////////////////////////
	// Documentation
	///////////////////////////////////////////////////////////////////////////

	public static VncFunction doc = new VncFunction("doc") {
		{
			setArgLists("(doc name)");
			
			setDoc("Returns the documentation for the function/macro with the given name");
		}
		public VncVal apply(final VncList args) {
			assertArity("doc", args, 1);
			
			return new VncString(Doc.getDoc(Coerce.toVncString(args.first()).getValue()));
		}
	};

	
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
				.put("doc",					doc)
							
				.toMap();

	
}
