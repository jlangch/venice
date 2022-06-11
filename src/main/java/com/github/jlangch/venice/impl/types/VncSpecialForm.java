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
package com.github.jlangch.venice.impl.types;

import com.github.jlangch.venice.impl.FunctionMetaBuilder;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.specialforms.util.SpecialFormsContext;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.util.MetaUtil;


public abstract class VncSpecialForm extends VncVal {

    public VncSpecialForm(final String name) {
        this(name, Constants.Nil);
    }

    public VncSpecialForm(final String name, final VncVal meta) {
        super(meta);

        this.name = name;
    }


    public VncVal apply(
            final VncVal specialFormMeta,
            final VncList args,
            final Env env,
            final SpecialFormsContext ctx
    ) {
        return Constants.Nil;
    }


    public String getName() {
        return name;
    }

    public boolean addCallFrame() {
        return true;
    }

    @Override
    public VncKeyword getType() {
        return new VncKeyword(
                TYPE,
                MetaUtil.typeMeta(
                    new VncKeyword(VncVal.TYPE)));
    }

    @Override
    public VncSpecialForm withMeta(final VncVal meta) {
        return this;  // no effect
    }

    public VncList getArgLists() {
        return (VncList)getMetaVal(MetaUtil.ARGLIST, VncList.empty());
    }

    public VncVal getDoc() {
        return getMetaVal(MetaUtil.DOC);
    }

    public VncList getExamples() {
        return (VncList)getMetaVal(MetaUtil.EXAMPLES, VncList.empty());
    }

    public VncList getSeeAlso() {
        return (VncList)getMetaVal(MetaUtil.SEE_ALSO, VncList.empty());
    }

    @Override
    public TypeRank typeRank() {
        return TypeRank.SPECIAL_FORM;
    }

    @Override
    public Object convertToJavaObject() {
        return null;
    }

    @Override
    public String toString() {
        return "special form " + name;
    }


    public static FunctionMetaBuilder meta() {
        return new FunctionMetaBuilder();
    }



    public static final String TYPE = ":core/special-form";

    private static final long serialVersionUID = -1848883965231344442L;

    private final String name;
}
