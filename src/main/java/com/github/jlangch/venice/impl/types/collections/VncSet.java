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
package com.github.jlangch.venice.impl.types.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.IVncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.ArityExceptions;


public abstract class VncSet extends VncCollection implements IVncFunction, Iterable<VncVal> {

	public VncSet(VncVal meta) {
		super(meta);
	}
	

	@Override
	public VncVal apply(final VncList args) {
		ArityExceptions.assertArity("set", args, 1, 2);
		
		final VncVal first = args.first();
		
		if (args.size() == 1) {
			return contains(first) ? first : Constants.Nil;
		}
		else {
			return contains(first) ? first : args.second();
		}
	}
	
	@Override
	public abstract VncSet emptyWithMeta();
	
	@Override
	public VncKeyword getType() {
		return TYPE;
	}
	
	@Override
	public VncKeyword getSupertype() {
		return VncCollection.TYPE;
	}

	@Override
	public List<VncKeyword> getAllSupertypes() {
		return Arrays.asList(VncCollection.TYPE, VncVal.TYPE);
	}

	public abstract VncSet withValues(Collection<? extends VncVal> replaceVals);
	
	public abstract VncSet withValues(Collection<? extends VncVal> replaceVals, VncVal meta);

	@Override
	public abstract VncSet withMeta(VncVal meta);
	

	public abstract VncSet add(VncVal val);
	
	public abstract VncSet addAll(VncSet val);
	
	public abstract VncSet addAll(VncSequence val);

	public abstract VncSet remove(VncVal val);

	public abstract VncSet removeAll(VncSet val);

	public abstract VncSet removeAll(VncSequence val);
	
	public abstract boolean contains(VncVal val);
	
	public abstract Set<VncVal> getJavaSet();
	
	public abstract List<VncVal> getJavaList();

    public abstract Iterator<VncVal> iterator();

	public abstract Stream<VncVal> stream();

	@Override
	public Object convertToJavaObject() {
		return stream()
				.map(v -> v.convertToJavaObject())
				.collect(Collectors.toSet());
	}


	public static final VncKeyword TYPE = new VncKeyword(":core/set");

    private static final long serialVersionUID = -1848883965231344442L;
}