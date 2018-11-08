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
package com.github.jlangch.venice;

import com.github.jlangch.venice.impl.types.VncVal;


/**
 * <b>Note:</b> 
 * 
 * <p>This class exposes <code>VncVal</code> an implementation type
 * that is not public. So it should be in the <code>impl</code> package.
 * 
 * <p>On the other hand when catching a <code>ValueException</code> in a Venice 
 * script it has to imported  <code>(import :com.github.jlangch.venice.ValueException)</code>.
 * But importing from an <code>impl</code> package is also not a good idea.
 *       
 * <p>TODO: find a good solution
 */
public class ValueException extends VncException {
		
	public ValueException(final VncVal value) {
		this.value = value;
	}

	public ValueException(final VncVal value, final Throwable cause) {
		super(cause);
		this.value = value;
	}
	
	public VncVal getValue() { 
		return value; 
	}

	
	private static final long serialVersionUID = -7070216020647646364L;

	private final VncVal value;
}