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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.github.jlangch.venice.impl.Env;


public class PreCompiled implements Serializable {
	
	public PreCompiled(final String name, final Object precompiled, final Env env) {
		this.name = name;
		this.precompiled = precompiled;
		this.env = env;
	}
	
	public String getName() {
		return name;
	}
	
	public Object getPrecompiled() {
		return precompiled;
	}
	
	public Env getEnv() {
		return env;
	}
	
	public byte[] serialize() {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			final ObjectOutput out = new ObjectOutputStream(bos);   
			out.writeObject(this);
			out.flush();
			return bos.toByteArray();
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to serialize pre-compile Venice script", ex);
		}
	}

	public  static PreCompiled deserialize(final byte[] precompiled) {
		try(ByteArrayInputStream bis = new ByteArrayInputStream(precompiled)) {
			final ObjectInputStream in = new ObjectInputStream(bis);   
			return (PreCompiled)in.readObject(); 
		} 
		catch (Exception ex) {
			throw new RuntimeException("Failed to deserialize pre-compile Venice script", ex);
		}
	}


	private static final long serialVersionUID = -3044466744877602703L;

	private final String name;
	private final Object precompiled;
	private final Env env;
}