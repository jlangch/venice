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
package com.github.jlangch.venice.util.excel;

import java.util.List;
import java.util.function.Function;

import com.github.jlangch.venice.impl.util.excel.ExcelColumnDef;


public class ExcelColumnBuilder<T> {

	public ExcelColumnBuilder(
			final ExcelSheetBuilder<T> excelSheetBuilder,
			final List<ExcelColumnDef<T>> columnDefs,
			final String name
	) {
		this.parentBuilder = excelSheetBuilder;
		this.columnDefs = columnDefs;
		this.name = name;
	}

	
	public ExcelColumnBuilder<T> colMapper(final Function<? super T, ?> mapper) {
		this.mapper = mapper;
		return this;
	}

	public ExcelColumnBuilder<T> widthInPoints(final int width) {
		this.width = width;
		return this;
	}
	
	public ExcelColumnBuilder<T> hidden(final boolean hide) {
		this.hidden = hide;
		return this;
	}

	public ExcelColumnBuilder<T> headerStyle(final String style) {
		this.headerStyle = style;
		return this;
	}
	
	public ExcelColumnBuilder<T> bodyStyle(final String style) {
		this.bodyStyle = style;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerStyle(final String style) {
		this.footerStyle = style;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerTextValue(final String text) {
		this.footerValue = text;
		footerType = ExcelColumnDef.FooterType.TEXT;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerNumberValue(final Number number) {
		this.footerValue = number;
		footerType = ExcelColumnDef.FooterType.NUMBER;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerMin() {
		this.footerValue = null;
		footerType = ExcelColumnDef.FooterType.MIN;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerMax() {
		this.footerValue = null;
		footerType = ExcelColumnDef.FooterType.MAX;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerAverage() {
		this.footerValue = null;
		footerType = ExcelColumnDef.FooterType.AVERAGE;
		return this;
	}
	
	public ExcelColumnBuilder<T> footerSum() {
		this.footerValue = null;
		footerType = ExcelColumnDef.FooterType.SUM;
		return this;
	}

	public ExcelSheetBuilder<T> end() {
		if (!hidden) {
			columnDefs.add(
				new ExcelColumnDef<T>(
						name, mapper, width, 
						headerStyle, bodyStyle, footerStyle, 
						footerValue, footerType));
		}
		return parentBuilder;
	}


	private final ExcelSheetBuilder<T> parentBuilder;
	private final List<ExcelColumnDef<T>> columnDefs;
	private Function<? super T, ?> mapper;
	private String name;
	private Integer width;
	private String headerStyle;
	private String bodyStyle;
	private String footerStyle;
	private Object footerValue;
	private boolean hidden = false;
	private ExcelColumnDef.FooterType footerType = ExcelColumnDef.FooterType.NONE;
}
