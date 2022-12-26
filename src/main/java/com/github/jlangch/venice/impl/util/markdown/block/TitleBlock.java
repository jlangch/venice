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
package com.github.jlangch.venice.impl.util.markdown.block;

import com.github.jlangch.venice.impl.util.StringUtil;


public class TitleBlock implements Block {

    public TitleBlock() {
        this("", 1);
    }

    public TitleBlock(final String text, final int level) {
        this.text = StringUtil.trimToEmpty(text);
        this.level = level;
    }


    public String getText() {
        return text;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public void parseChunks() {
    }


    private final String text;
    private final int level;
}
