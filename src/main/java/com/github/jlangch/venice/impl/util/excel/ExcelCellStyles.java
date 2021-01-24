/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * Manages cell formats
 * 
 * <p>dynamic row height
 *     HSSFCellStyle style = workbook.createCellStyle();
 *     style.setWrapText(true);
 * 
 * @author juerg
 */
public class ExcelCellStyles {

	public ExcelCellStyles(final Workbook workbook, final Map<String,Font> fonts) {
		this.workbook = workbook;
		this.fonts = fonts;
		this.dataFormat = workbook.createDataFormat();
		registerStandardFormats();
	}

	public void registerCellFormat(
			final String name,
			final String dataFormat,
			final String fontRefName,
			final Short bgColorIndex,
			final Boolean wrapText,
			final HorizontalAlignment hAlign,
			final VerticalAlignment vAlign
	) {
		if (name == null) {
			throw new IllegalArgumentException("A cell format name must not be null");
		}
		
		final CellStyle style = workbook.createCellStyle();
		
		if (bgColorIndex != null) {
			style.setFillForegroundColor(bgColorIndex);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		if (dataFormat != null) {
			style.setDataFormat(this.dataFormat.getFormat(dataFormat));
		}
		if (fontRefName != null) {
			final Font font = fonts.get(fontRefName);
			if (font != null) {
				style.setFont(font);
			}
		}
		if (wrapText != null) {
			style.setWrapText(wrapText);
		}
		if (hAlign != null) {
			style.setAlignment(hAlign);
		}
		if (vAlign != null) {
			style.setVerticalAlignment(vAlign);
		}

		cellStyles.put(name, style);
	}

	public void registerCellFormat(
			final String name,
			final String dataFormat,
			final String fontRefName,
			final Color bgColor,
			final Boolean wrapText,
			final HorizontalAlignment hAlign,
			final VerticalAlignment vAlign
	) {
		if (name == null) {
			throw new IllegalArgumentException("A cell format name must not be null");
		}
		
		final CellStyle style = workbook.createCellStyle();
		
		if (bgColor != null) {
			if (workbook instanceof XSSFWorkbook) {
				((XSSFCellStyle)style).setFillForegroundColor(
						new XSSFColor(bgColor, null));
			}
			else if (workbook instanceof HSSFWorkbook) {
				final HSSFColor hssfColor = ColorUtil.bestHSSFColor((HSSFWorkbook)workbook, bgColor);
				style.setFillForegroundColor(hssfColor.getIndex());
			}
			
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		if (dataFormat != null) {
			style.setDataFormat(this.dataFormat.getFormat(dataFormat));
		}
		if (fontRefName != null) {
			final Font font = fonts.get(fontRefName);
			if (font != null) {
				style.setFont(font);
			}
		}
		if (wrapText != null) {
			style.setWrapText(wrapText);
		}
		if (hAlign != null) {
			style.setAlignment(hAlign);
		}
		if (vAlign != null) {
			style.setVerticalAlignment(vAlign);
		}

		cellStyles.put(name, style);
	}

	public CellStyle getCellStyle(final String name) {
		return (name != null) ? cellStyles.get(name) : null;
	}
	
	private void registerStandardFormats() {
		getStandardFormats()
			.entrySet()
			.stream()
			.filter(e -> e.getValue() != null)
			.forEach(e -> registerCellFormat(e.getKey(), e.getValue(), null, (Short)null, null, null, null));	
	}

	private Map<String,String> getStandardFormats() {
		final Map<String,String> cellDataFormats = new HashMap<>();		
		cellDataFormats.put("string", null);
		cellDataFormats.put("boolean", null);
		cellDataFormats.put("integer", "#0");
		cellDataFormats.put("float", "#,##0.00");
		cellDataFormats.put("date", "d.m.yyyy");
		cellDataFormats.put("datetime", "d.m.yyyy hh:mm:ss");
		return cellDataFormats;
	}

	
	private final Workbook workbook;
	private final Map<String,Font> fonts;
	private final DataFormat dataFormat;
	private final Map<String,CellStyle> cellStyles = new HashMap<>();
}
