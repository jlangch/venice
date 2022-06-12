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
package com.github.jlangch.venice.impl.types.custom;

import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashSet;
import com.github.jlangch.venice.impl.types.collections.VncMap;
import com.github.jlangch.venice.impl.types.collections.VncOrderedMap;
import com.github.jlangch.venice.impl.types.collections.VncSet;


public class VncChoiceTypeDef extends VncCustomBaseTypeDef {

    public VncChoiceTypeDef(
            final VncKeyword type,
            final VncSet choiceTypes,
            final VncSet choiceValues
    ) {
        super(type);

        this.choiceTypes = choiceTypes;
        this.choiceValues = choiceValues;
    }


    public boolean isChoice(final VncVal val) {
        return isChoiceType(val) || isChoiceValue(val);
    }

    public boolean isChoiceType(final VncVal val) {
        return choiceTypes.contains(val);
    }

    public boolean isChoiceValue(final VncVal val) {
        return choiceValues.contains(val);
    }

    public VncSet typesOnly() {
        return choiceTypes;
    }

    public VncSet valuesOnly() {
        return choiceValues;
    }

    public VncSet values() {
        return new VncHashSet()
                    .addAll(choiceTypes.toVncList())
                    .addAll(choiceValues.toVncList());
    }

    @Override
    public VncMap toMap() {
        return VncOrderedMap.of(
                new VncKeyword(":type"),		getType(),
                new VncKeyword(":custom-type"), new VncKeyword(":choice"),
                new VncKeyword(":values"),		values());
    }


    private static final long serialVersionUID = -1848883965231344442L;

    private final VncSet choiceTypes;
    private final VncSet choiceValues;
}
