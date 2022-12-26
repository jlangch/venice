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
package com.github.jlangch.venice.impl.debug.breakpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.types.util.QualifiedName;
import com.github.jlangch.venice.impl.util.CollectionUtil;


/**
 * Defines a breakpoint given by a qualified function name and optional
 * selectors for the function scope and the function's ancestor.
 */
public class BreakpointFn implements Comparable<BreakpointFn> {

    public BreakpointFn(
            final QualifiedName qualifiedName
    ) {
        this(qualifiedName, new ArrayList<>());
    }

    public BreakpointFn(
            final QualifiedName qualifiedName,
            final Selector selector
    ) {
        this(qualifiedName, CollectionUtil.toList(selector));
    }

    public BreakpointFn(
            final QualifiedName qualifiedName,
            final List<Selector> selectors
    ) {
        if (qualifiedName == null) {
            throw new IllegalArgumentException("A qualifiedName must not be null");
        }

        this.ref = new BreakpointFnRef(qualifiedName.getQualifiedName());
        this.qn = qualifiedName;
        this.selectors = new ArrayList<>();

        if (selectors == null || selectors.isEmpty()) {
            this.selectors.add(new Selector());
        }
        else {
            this.selectors.addAll(selectors);
        }
    }

    public BreakpointFn merge(final List<Selector> newSelectors) {
        if (newSelectors == null || newSelectors.isEmpty()) {
            return this;
        }
        else {
            List<Selector> mergedSelectors = new ArrayList<>(selectors);

            for(Selector other : newSelectors) {
                Selector match = findSelectorByMatchingAncestor(other, mergedSelectors);
                if (match != null) {
                    // remove selector match
                    mergedSelectors = mergedSelectors
                                        .stream()
                                        .filter(s -> !s.hasSameAncestorSelector(match))
                                        .collect(Collectors.toList());

                }
                mergedSelectors.add(other);
            }

            return new BreakpointFn(qn, mergedSelectors);
        }
    }

    public String getQualifiedFnName() {
        return qn.getQualifiedName();
    }

    public String getNamespace() {
        return qn.getNamespace();
    }

    public String getSimpleFnName() {
        return qn.getSimpleName();
    }

    public List<Selector> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    public BreakpointFnRef getBreakpointRef() {
        return ref;
    }

    public List<String> format(boolean useDescriptiveScopeNames) {
        return selectors
                .stream()
                .map(s -> s.formatForBaseFn(
                                qn.getQualifiedName(),
                                useDescriptiveScopeNames))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return format(false)
                .stream()
                .collect(Collectors.joining("\n"));
    }

    @Override
    public int hashCode() {
        return qn.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BreakpointFn other = (BreakpointFn) obj;
        return (qn.equals(other.qn));
    }

    @Override
    public int compareTo(final BreakpointFn o) {
        return comp.compare(this, o);
    }

    private Selector findSelectorByMatchingAncestor(
            final Selector candidate,
            final List<Selector> selectors
    ) {
        return selectors
                .stream()
                .filter(s -> s.hasSameAncestorSelector(candidate))
                .findFirst()
                .orElse(null);
    }


    private static Comparator<BreakpointFn> comp =
            Comparator.comparing(BreakpointFn::getQualifiedFnName);

    private final BreakpointFnRef ref;
    private final QualifiedName qn;
    private final List<Selector> selectors;
}
