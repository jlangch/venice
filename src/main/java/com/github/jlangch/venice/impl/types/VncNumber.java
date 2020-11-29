/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;


public abstract class VncNumber extends VncVal {

	public VncNumber(final VncVal meta) {	
		super(null, meta);
	}

	public VncNumber(
			final VncWrappingTypeDef wrappingTypeDef, 
			final VncVal meta
	) {	
		super(wrappingTypeDef, meta);
	}
	

	public abstract Integer toJavaInteger();
	public abstract Long toJavaLong();
	public abstract Double toJavaDouble();
	public abstract BigInteger toJavaBigInteger();
	public abstract BigDecimal toJavaBigDecimal();
	public abstract BigDecimal toJavaBigDecimal(final int scale);

	
    private static final long serialVersionUID = -1848883965231344442L;
}
