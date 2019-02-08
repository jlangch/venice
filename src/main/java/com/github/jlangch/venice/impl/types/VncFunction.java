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
package com.github.jlangch.venice.impl.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncVector;


public abstract class VncFunction extends VncVal implements IVncFunction {

	public VncFunction() {
		this(null, null, null, null);
	}
	
	public VncFunction(final String name) {
		this(name, null, null, null);
	}
	
	public VncFunction(final String name, final VncVal meta) {
		this(name, null, null, null, meta);
	}

	public VncFunction(final VncVal ast, final Env env, final VncVector params) {
		this(null, ast, env, params);
	}

	public VncFunction(final String name, final VncVal ast, final Env env, final VncVector params) {
		this(name, ast, env, params, Constants.Nil);
	}

	public VncFunction(final String name, final VncVal ast, final Env env, final VncVector params, final VncVal meta) {
		super(Constants.Nil);
		this.name = name == null ? createAnonymousFuncName() : name;
		this.ast = ast;
		this.env = env;
		this.params = params;
		
		this.fnMeta.set(meta);
	}

	
	@Override
	public VncFunction withMeta(final VncVal meta) {
		this.fnMeta.set(meta);
		return this;
	}

	
	public VncVal getAst() { 
		return ast; 
	}
	
	public Env getEnv() { 
		return env; 
	}
	
	public VncVector getParams() { 
		return params; 
	}
	
	public Env genEnv(final VncList args) {
		return new Env(env).addAll(Destructuring.destructure(params, args));
	}
	
	public boolean isMacro() { 
		return macro; 
	}
	
	public void setMacro() { 
		macro = true; 
	}
	
	public String getName() { 
		return name; 
	}

	public static String createAnonymousFuncName() {
		return "anonymous-" + UUID.randomUUID().toString();
	}

	public VncList getArgLists() { 
		return (VncList)getMetaVal(MetaUtil.ARGLIST, new VncList());
	}
	
	public VncVal getDoc() { 
		return getMetaVal(MetaUtil.DOC); 
	}
	
	public VncList getExamples() { 
		return (VncList)getMetaVal(MetaUtil.EXAMPLES, new VncList());
	}
	
	@Override public int typeRank() {
		return 100;
	}

	@Override 
	public String toString() {
		return name;
	}

	public VncVal getMeta() { 
		return fnMeta.get(); 
	}

	public static MetaBuilder meta() {
		return new MetaBuilder();
	}
	
	
	public static class MetaBuilder  {

		public MetaBuilder() {
		}
		
		public MetaBuilder arglists(final String... arglists) {
			meta.put(
				MetaUtil.ARGLIST, 
				new VncList(Arrays.stream(arglists).map(s -> new VncString(s)).collect(Collectors.toList())));
			return this;
		}
		
		public MetaBuilder doc(final String doc) { 
			meta.put(MetaUtil.DOC, new VncString(doc));
			return this;
		}
		
		public MetaBuilder examples(final String... examples) { 
			meta.put(
				MetaUtil.EXAMPLES, 
				new VncList(Arrays.stream(examples).map(s -> new VncString(s)).collect(Collectors.toList())));
			return this;
		}
		
		public VncHashMap build() {
			return new VncHashMap(meta);
		}

		private final HashMap<VncVal,VncVal> meta = new HashMap<>();
	}
	

    private static final long serialVersionUID = -1848883965231344442L;

	private final VncVal ast;
	private final Env env;
	private final VncVector params;
	private volatile boolean macro = false;
	private final String name;
	
	// Functions handle its meta data localy (functions cannot be copied)
	private final AtomicReference<VncVal> fnMeta = new AtomicReference<>(Constants.Nil);
}