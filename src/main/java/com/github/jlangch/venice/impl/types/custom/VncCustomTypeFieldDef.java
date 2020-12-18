/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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

import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncKeyword;

public class VncCustomTypeFieldDef  {

	public VncCustomTypeFieldDef(
			final VncKeyword name,
			final VncKeyword type,
			final VncInteger index
	) {
		this.name = name;
		this.type = type;
		this.index = index;
	}
	
		
	public VncKeyword getName() {
		return name;
	}
	
	public VncKeyword getType() {
		return type;
	}
	
	public VncInteger getIndex() {
		return index;
	}


	private final VncKeyword name;
	private final VncKeyword type;
	private final VncInteger index;
}
