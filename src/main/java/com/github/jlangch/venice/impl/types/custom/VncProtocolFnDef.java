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
package com.github.jlangch.venice.impl.types.custom;

import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public class VncProtocolFnDef  {

	public VncProtocolFnDef(
			final VncString name,
			final VncVector params,
			final VncVal retVal
	) {
		this.name = name;
		this.params = params;
		this.retVal = retVal;
	}
	
		
	public VncString getName() {
		return name;
	}
	
	public VncVector getParams() {
		return params;
	}
	
	public VncVal getRetVal() {
		return retVal;
	}

	@Override
	public String toString() {
		return String.format(
				"(%s %s)", 
				name.getValue(),
				params.toString());
	}
	
	
	private final VncString name;
	private final VncVector params;
	private final VncVal retVal;
}
