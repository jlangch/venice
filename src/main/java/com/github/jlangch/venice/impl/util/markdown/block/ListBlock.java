/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import java.util.ArrayList;
import java.util.List;


public class ListBlock implements Block {

    public ListBlock() {
    }

    public void addItem(final Block block) {
        if (block != null) {
            items.add(block);
        }
    }

    public List<Block> getItems() {
        return items;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Block get(final int index) {
        return items.get(index);
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(final boolean ordered) {
        this.ordered = ordered;
    }

    @Override
    public void parseChunks() {
        items.forEach(b -> b.parseChunks());
    }


    private List<Block> items = new ArrayList<>();
    private boolean ordered = false;
}
