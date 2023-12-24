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
package com.github.jlangch.venice.impl.util.excel;

import java.util.function.Function;

import com.github.jlangch.venice.impl.util.StringUtil;


public class ExcelColumnDef<T> {

    public ExcelColumnDef(
            final String header,
            final Function<? super T, ?> colMapper,
            final Integer width,
            final String headerStyle,
            final String bodyStyle,
            final String footerStyle,
            final Object footerValue,
            final FooterType footerType
    ) {
        this.header = StringUtil.trimToEmpty(header);
        this.colMapper = colMapper;
        this.width = width;
        this.headerStyle = headerStyle;
        this.bodyStyle = bodyStyle;
        this.footerStyle = footerStyle;
        this.footerValue = footerValue;
        this.footerType = footerType == null ? ExcelColumnDef.FooterType.NONE : footerType;
    }


    public static enum FooterType {
        NONE, NUMBER, TEXT, FORMULA, SUM, MIN, MAX, AVERAGE;
    }


    public final String header;
    public final Function<? super T, ?> colMapper;
    public final Integer width;
    public final String headerStyle;
    public final String bodyStyle;
    public final String footerStyle;
    public final Object footerValue;
    public final FooterType footerType;
}
