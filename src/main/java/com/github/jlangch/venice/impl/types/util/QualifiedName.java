/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2023 Venice
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

import static com.github.jlangch.venice.impl.util.StringUtil.trimToNull;

import com.github.jlangch.venice.VncException;


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
        final String namespace_ = trimToNull(namespace);
        final String simpleName_ = trimToNull(simpleName);

        // validate
        if (namespace_ != null && namespace_.indexOf("/") >= 0) {
            throw new VncException(String.format(
                        "A namespace ('%s') must not contain a '/'",
                        namespace_));
        }
        if (simpleName_ == null) {
            throw new VncException(
                    "A simple name of a qualified name must not be blank");
        }
        else if (!simpleName_.equals("/") && simpleName_.indexOf("/") >= 0) {
            throw new VncException(String.format(
                    "A simple name ('%s') of a qualified name must not contain a '/'",
                    simpleName_));
        }

        final String qualifiedName_ = namespace_ == null
                                        ? simpleName_
                                        : namespace_ + "/" + simpleName_;

        return new QualifiedName(qualifiedName_, namespace_, simpleName_);
    }

    public static QualifiedName parse(final String name) {
        final String name_ = trimToNull(name);
        if (name_ == null) {
            throw new VncException("A name must not be blank");
        }

        if (name_.equals("/")) {
            // special case core function "/" (division)
            return new QualifiedName("/", null, "/");
        }
        else if (name_.equals("core//")) {
            // special case core function "/" (division)
            return new QualifiedName("core//", "core", "/");
        }
        else {
            final int pos = name_.lastIndexOf("/");

            final String namespace = pos < 0
                                        ? null
                                        : trimToNull(name_.substring(0, pos));

            final String simpleName = pos < 0
                                        ? name_
                                        : trimToNull(name_.substring(pos+1));

            // validate
            if (namespace != null && namespace.indexOf("/") >= 0) {
                throw new VncException(String.format(
                                "A namespace ('%s') of a qualified name ('%s') "
                                + "must not contain a '/'",
                                namespace,
                                name));
            }
            if (simpleName == null) {
                throw new VncException(String.format(
                            "A simple name ('%s') of a qualified name ('%s') "
                            + "name must not be blank",
                            simpleName,
                            name));
            }

            final String qualifiedName = namespace == null
                                            ? simpleName
                                            : namespace + "/" + simpleName;

            return new QualifiedName(qualifiedName, namespace, simpleName);
        }
    }

    public QualifiedName withOtherNamespace(final String namespace) {
        return QualifiedName.of(namespace, simpleName);
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

    public boolean isQualified() {
    	return namespace != null;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QualifiedName other = (QualifiedName) obj;
        if (namespace == null) {
            if (other.namespace != null)
                return false;
        } else if (!namespace.equals(other.namespace))
            return false;
        if (qualifiedName == null) {
            if (other.qualifiedName != null)
                return false;
        } else if (!qualifiedName.equals(other.qualifiedName))
            return false;
        if (simpleName == null) {
            if (other.simpleName != null)
                return false;
        } else if (!simpleName.equals(other.simpleName))
            return false;
        return true;
    }


    private final String qualifiedName;
    private final String namespace;
    private final String simpleName;
}
