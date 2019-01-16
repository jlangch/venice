/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl.types.collections;

import java.util.Arrays;
import java.util.Collection;

import com.github.jlangch.venice.impl.Printer;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.Types;
import com.github.jlangch.venice.impl.types.VncVal;


public class VncVector extends VncList {

	public VncVector() {
	}

	public VncVector(Collection<? extends VncVal> vals) {
		super(vals);
	}
	
	
	public static VncVector ofAll(final VncVal... mvs) {
		return new VncVector(Arrays.asList(mvs));
	}

	
	@Override
	public VncVector empty() {
		return copyMetaTo(new VncVector());
	}
	
	@Override
	public VncVector copy() {
		return copyMetaTo(new VncVector(toVncList().copy().getList()));
	}

	@Override
	public boolean isList() { 
		return false; 
	}
	
	@Override
	public VncVector rest() {
		return isEmpty() ? new VncVector() : new VncVector(getList().subList(1, getList().size()));
	}

	@Override
	public VncVector slice(final int start) {
		return slice(start, getList().size());
	}

	@Override
	public VncVector slice(final int start, final int end) {
		return new VncVector(getList().subList(start, end));
	}
	
	@Override
	public VncList toVncList() {
		return copyMetaTo(new VncList(getList()));
	}

	@Override
	public VncVector toVncVector() {
		return this;
	}

	@Override
	public int compareTo(final VncVal o) {
		if (o == Constants.Nil) {
			return 1;
		}
		else if (Types.isVncList(o)) {
			for(int ii=0; ii<Math.min(size(), ((VncList)o).size()); ii++) {
				int c = nth(ii).compareTo(((VncList)o).nth(ii));
				if (c != 0) {
					return c;
				}
			}
			
		}
		
		return 0;
	}

	@Override 
	public String toString() {
		return "[" + Printer.join(getList(), " ", true) + "]";
	}
	
	public String toString(final boolean print_readably) {
		return "[" + Printer.join(getList(), " ", print_readably) + "]";
	}


	private static final long serialVersionUID = -1848883965231344442L;
}