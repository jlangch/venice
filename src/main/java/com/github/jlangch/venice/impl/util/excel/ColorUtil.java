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

import java.awt.Color;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;

public class ColorUtil {

    public static HSSFColor bestHSSFColor(final HSSFWorkbook wb, final Color color) {
        final HSSFPalette palette = wb.getCustomPalette();
        final HSSFColor hssfColor = palette.findColor(
                                        (byte)color.getRed(),
                                        (byte)color.getGreen(),
                                        (byte)color.getBlue());
        if (hssfColor != null) {
            return hssfColor;
        }
        else {
            final HSSFColor hssfColor2 = palette.findSimilarColor(
                                            (byte)color.getRed(),
                                            (byte)color.getGreen(),
                                            (byte)color.getBlue());

            return hssfColor2 != null ? hssfColor2 : HSSFColorPredefined.AUTOMATIC.getColor();
        }

    }

}
