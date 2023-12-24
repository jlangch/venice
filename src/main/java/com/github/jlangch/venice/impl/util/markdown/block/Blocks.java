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


public class Blocks {

    public Blocks() {
    }

    public Blocks add(final Block block) {
        if (block != null && !block.isEmpty()) {
            blocks.add(block);
        }
        return this;
    }

    public Blocks add(final Blocks blocks) {
        if (blocks != null ) {
            for(Block b : blocks.getBlocks()) {
                if (!b.isEmpty()) this.blocks.add(b);
            }
        }

        return this;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public int size() {
        return blocks.size();
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Block get(final int index) {
        return blocks.get(index);
    }


    private final List<Block> blocks = new ArrayList<>();
}
