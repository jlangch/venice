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
package com.github.jlangch.venice.impl.util.markdown.block;


public class TableColFmt {

    public TableColFmt() {
        this(null, null);
    }

    public TableColFmt(
            final HorzAlignment align,
            final Width width
    ) {
        this.horzAlignment = align == null ? HorzAlignment.LEFT : align;
        this.width = width == null ? new Width(0, WidthUnit.AUTO) : width;
    }


    public HorzAlignment horzAlignment() {
        return horzAlignment;
    }

    public Width width() {
        return width;
    }


    public static enum HorzAlignment { LEFT, CENTER, RIGHT };
    public static enum WidthUnit { PERCENT, PX, EM, AUTO };

    public static class Width {
        public Width(long value, WidthUnit unit) {
            this.value = value;
            this.unit = unit;
        }

        public long getValue() { return value; }
        public WidthUnit getUnit() { return unit; }

        private final long value;
        private final WidthUnit unit;
    }


    private final HorzAlignment horzAlignment;
    private final Width width;
}
