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
package com.github.jlangch.venice.impl.javainterop;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jlangch.venice.impl.VeniceInterpreter;

// https://medium.com/@isuru89/java-a-child-first-class-loader-cbd9c3d0305
public class DynamicClassLoader extends URLClassLoader {
	
	public DynamicClassLoader() {
		super(EMPTY_URLS, getParentClassLoader());
	}
	
	public DynamicClassLoader(final ClassLoader parent) {
		super(EMPTY_URLS, parent);
	}
	
	public Class<?> defineClass(
			final String name, 
			final byte[] bytes, 
			final Object srcForm
	) {
		clearCache(refQueue, classCache);
		final Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
		classCache.put(name, new SoftReference<Class<?>>(clazz, refQueue));
		return clazz;
	}
	
	@Override
	protected Class<?> findClass(final String name) 
	throws ClassNotFoundException {
		final Class<?> clazz = findInMemoryClass(name);
		return clazz != null ? clazz : super.findClass(name);
	}
	
	@Override
	protected synchronized Class<?> loadClass(
			final String name, 
			final boolean resolve
	) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz == null) {
			clazz = findInMemoryClass(name);
			if (clazz == null) {
				clazz = super.loadClass(name, false);
			}
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}
		
	@Override
	public void addURL(final URL url) {
		super.addURL(url);
	}

	public static void clearCache(
			final ReferenceQueue<Class<?>> rq, 
			final ConcurrentHashMap<String, Reference<Class<?>>> cache
	) {
		//cleanup any dead entries
		if (rq.poll() != null) {
			while(rq.poll() != null) ;
			
			for(Map.Entry<String, Reference<Class<?>>> e : cache.entrySet()) {
				final Reference<Class<?>> val = e.getValue();
				if (val != null && val.get() == null)
					cache.remove(e.getKey(), val);
				}
			}
	}

	
	private static Class<?> findInMemoryClass(final String name) {
		final Reference<Class<?>> classRef = classCache.get(name);
		if (classRef != null) {
			final Class<?> clazz = classRef.get();
			if (clazz != null) {
				return clazz;
			}
			else {
				classCache.remove(name, classRef);
			}
		}
		return null;
	}

	private static ClassLoader getParentClassLoader() {
		final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		
		return ctxClassLoader == null || ctxClassLoader == sysClassLoader 
				? VeniceInterpreter.class.getClassLoader()
				: ctxClassLoader;
	}
	
	
	
	private static final ConcurrentHashMap<String, Reference<Class<?>>> classCache = new ConcurrentHashMap<>();
	
	private static final URL[] EMPTY_URLS = new URL[]{};
	
	private static final ReferenceQueue<Class<?>> refQueue = new ReferenceQueue<>();
}