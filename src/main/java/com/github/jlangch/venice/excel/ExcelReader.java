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
package com.github.jlangch.venice.excel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.github.jlangch.venice.impl.util.excel.Excel;


/**
 * An Excel reader
 * 
 * @author juerg
 */
public class ExcelReader {

	private ExcelReader(
		final Excel excel
	) {
		this.excel = excel;
	}

	
	public static ExcelReader open(final byte[] document) {
		return new ExcelReader(Excel.open(new ByteArrayInputStream(document)));
	}
	
	public static ExcelReader open(final ByteBuffer document) {
		return new ExcelReader(Excel.open(new ByteArrayInputStream(document.array())));
	}
	
	public static ExcelReader open(final File file) {
		return new ExcelReader(Excel.open(file));
	}
	
	public static ExcelReader open(final InputStream is) {
		return new ExcelReader(Excel.open(is));
	}

	

	public int getNumberOfSheets() {
		return excel.getNumberOfSheets();
	}
	
	public void evaluateAllFormulas() {
		excel.evaluateAllFormulas();
	}

	public ExcelSheetReader getSheet(final String name) {
		return new ExcelSheetReader(excel.getSheet(name));
	}

	public ExcelSheetReader getSheetAt(final int sheetIdx) {
		return new ExcelSheetReader(excel.getSheetAt(sheetIdx));
	}
	
	

	private final Excel excel;
}
