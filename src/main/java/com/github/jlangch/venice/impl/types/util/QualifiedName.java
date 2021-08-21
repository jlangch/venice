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
package com.github.jlangch.venice.impl.types.util;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.util.StringUtil;


public class QualifiedName {
	
	private QualifiedName(
			final String qualifiedName,
			final String namespace, 
			final String simpleName
	) {
		this.qualifiedName = qualifiedName;
		this.namespace = namespace;
		this.simpleName = simpleName;
	}

	
	public static QualifiedName of(
			final String namespace, 
			final String simpleName
	) {
		String namespace_ = StringUtil.trimToNull(namespace);
		if (namespace_.indexOf("/") >= 0) {
			throw new VncException("A namespace must not contain a '/'");
		}
		namespace_ = namespace_ == null ? "core" : namespace_;
		
		final String simpleName_ = StringUtil.trimToNull(namespace);
		if (simpleName_ == null) {
			throw new VncException("A simpleName must not be blank");
		}
		if (simpleName_.indexOf("/") >= 0) {
			throw new VncException("A simpleName must not contain a '/'");
		}
			
		final String qualifiedName_ = "core".equals(namespace) 
										? simpleName 
										: namespace + "/" + simpleName;

		return new QualifiedName(
						qualifiedName_, 
						namespace_, 
						simpleName_);
	}

	public static QualifiedName parse(final String name) {
		final String name_ = StringUtil.trimToNull(name);
		if (name_ == null) {
			throw new VncException("A qualified name must not be blank");
		}

		if (name_.equals("/")) {
			// special case function
			return new QualifiedName("/", "core", "/");
		}
		else {
			final int pos = name_.indexOf("/");
			
			String namespace = pos < 0 ? null : StringUtil.trimToNull(name_.substring(0, pos));
			namespace = namespace == null ? "core" : namespace;
			
			String simpleName = pos < 0 ? name_ : StringUtil.trimToNull(name_.substring(pos+1));
			if (simpleName == null) {
				throw new VncException("A simple name of a qualified name name must not be blank");
			}
			
			final String qualifiedName = "core".equals(namespace) 
											? simpleName 
											: namespace + "/" + simpleName;

			return new QualifiedName(
						qualifiedName, 
						namespace, 
						simpleName);
		}
	}

	public static QualifiedName parseWithoutCoreNamespaceMapping(final String name) {
		final String name_ = StringUtil.trimToNull(name);
		if (name_ == null) {
			throw new VncException("A qualified name must not be blank");
		}

		if (name_.equals("/")) {
			// special case function
			return new QualifiedName("/", null, "/");
		}
		else {
			final int pos = name_.indexOf("/");
			
			String namespace = pos < 0 ? null : StringUtil.trimToNull(name_.substring(0, pos));
			
			String simpleName = pos < 0 ? name_ : StringUtil.trimToNull(name_.substring(pos+1));
			if (simpleName == null) {
				throw new VncException("A simple name of a qualified name name must not be blank");
			}
			
			final String qualifiedName = namespace == null
											? simpleName 
											: namespace + "/" + simpleName;

			return new QualifiedName(
						qualifiedName, 
						namespace, 
						simpleName);
		}
	}
	
	public QualifiedName withOtherNamespace(final String namespace) {
		return QualifiedName.of(namespace, simpleName);
	}
	
	public QualifiedName mapCoreNamespaceToNull() {
		return new QualifiedName(
					qualifiedName, 
					"core".equals(namespace) ? null : namespace, 
					simpleName);
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getSimpleName() {
		return simpleName;
	}


	private final String qualifiedName;
	private final String namespace;
	private final String simpleName;
}
