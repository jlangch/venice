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
package com.github.jlangch.venice.impl.types;

import java.util.UUID;
import java.util.function.Function;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.Destructuring;
import com.github.jlangch.venice.impl.Env;
import com.github.jlangch.venice.impl.MetaUtil;
import com.github.jlangch.venice.impl.types.collections.VncList;


public abstract class VncFunction extends VncVal implements Function<VncList, VncVal>, java.lang.Cloneable {

	public VncFunction() {
		this(null, null, null, null);
	}
	
	public VncFunction(final String name) {
		this(name, null, null, null);
	}

	public VncFunction(final VncVal ast, final Env env, final VncList params) {
		this(null, ast, env, params);
	}

	public VncFunction(final String name, final VncVal ast, final Env env, final VncList params) {
		this.name = name == null ? createAnonymousFuncName() : name;
		this.ast = ast;
		this.env = env;
		this.params = params;
	}
	
	public VncFunction copy() {
		try {
			final VncFunction v = (VncFunction)this.clone();
			v.ast = ast;
			v.env = env;
			v.params = params;
			v.macro = macro;
			v.setMeta(getMeta());
			return v;
		} 
		catch (Exception ex) {
			 throw new VncException("Could not copy VncFunction: " + this, ex);
		}
	}

	public VncVal getAst() { 
		return ast; 
	}
	
	public Env getEnv() { 
		return env; 
	}
	
	public VncList getParams() { 
		return params; 
	}
	
	public Env genEnv(final VncList args) {
		final Env localEnv = new Env(env);

		Destructuring
			.destructure(params, args)
			.forEach(b -> localEnv.set(b.sym, b.val));

		return localEnv;
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
	
	public void setArgLists(final String... arglists) {
		MetaUtil.setArgList(this, arglists);
	}
	
	public VncVal getDescription() { 
		return getMetaVal(MetaUtil.DOC); 
	}
	
	public void setDescription(final String description) { 
		MetaUtil.setDoc(this, description);
	}
	
	public VncList getExamples() { 
		return (VncList)getMetaVal(MetaUtil.EXAMPLES, new VncList());
	}
	
	public void setExamples(final String... examples) { 
		MetaUtil.setExamples(this, examples);
	}

	@Override 
	public String toString() {
		return name;
	}
	

	public VncVal ast;
	public Env env;
	public VncList params;
	public boolean macro = false;
	public final String name;
}