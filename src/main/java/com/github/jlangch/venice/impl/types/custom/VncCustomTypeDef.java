/*   __	__		 _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *	\ \/ / _ \ '_ \| |/ __/ _ \
 *	 \  /  __/ | | | | (_|  __/
 *	  \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jlangch.venice.impl.types.custom;

import java.util.List;

import com.github.jlangch.venice.AssertionException;
import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncBoolean;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;


public class VncCustomTypeDef extends VncCustomBaseTypeDef {

	public VncCustomTypeDef(
			final VncKeyword type,
			final List<VncCustomTypeFieldDef> fieldDefs,
			final VncFunction validationFn
	) {
		super(type);
		
		this.fieldDefs = fieldDefs;
		this.validationFn = validationFn;
	}


	public VncCustomTypeFieldDef getFieldDef(final int index) {
		if (index >= 0 && index < fieldDefs.size()) {
			return fieldDefs.get(index);
		}
		else {
			throw new VncException(String.format(
					"deftype: field def index %d out of bounds.", index)); 
		}
	}

	public List<VncCustomTypeFieldDef> getFieldDefs() {
		return fieldDefs;
	}

	public VncFunction getValidationFn() {
		return validationFn;
	}
	
	public int count() {
		return fieldDefs.size();
	}

	public void validate(final VncVal val) {
		if (validationFn != null) {
			try {
				final VncVal valid = validationFn.apply(VncList.of(val));
				if (valid == Constants.Nil || VncBoolean.isFalse(valid)) {
					throw new AssertionException(String.format(
							"Invalid value for custom type :%s",
							getType().getValue()));
				}
			}
			catch(AssertionException ex) {
				throw ex;
			}
			catch(Exception ex) {
				throw new AssertionException(
						String.format(
								"Invalid value for custom type :%s",
								getType().getValue()),
						ex);
			}
		}
	}
	

    private static final long serialVersionUID = -1848883965231344442L;	

	private final List<VncCustomTypeFieldDef> fieldDefs;
	private final VncFunction validationFn;
}
