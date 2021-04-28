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
package com.github.jlangch.venice.impl.docgen.cheatsheet;

import java.util.ArrayList;
import java.util.List;


public class DocSection {
	
	public DocSection(final String title, final String id) {
		this(title, id, (String)null, (String)null);
	}
	
	public DocSection(
			final String title, 
			final String id, 
			final String header, 
			final String footer
	) {
		this.title = title;
		this.id = id;
		if (header != null) {
			this.headers.add(header);
		}
		if (footer != null) {
			this.footers.add(footer);
		}
	}
	
	public DocSection(
			final String title, 
			final String id, 
			final List<String> headers, 
			final List<String> footers
	) {
		this.title = title;
		this.id = id;
		if (headers != null) {
			this.headers.addAll(headers);
		}
		if (footers != null) {
			this.footers.addAll(footers);
		}
	}
	
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
	}
	
	public List<String> getHeaders() {
		return headers;
	}
	
	public List<String> getFooters() {
		return footers;
	}
	
	public void addSection(final DocSection section) {
		if (section != null) {
			sections.add(section);
		}
	}
	
	public void addLiteralIem(final String name, final String text) {
		final DocSection s = new DocSection(name, null);
		addSection(s);
		s.addItem(new DocItem(text, null));
	}
	
	public List<DocSection> getSections() {
		return sections;
	}
	
	public boolean isSectionsEmpty() {
		return sections.isEmpty();
	}
	
	public void addItem(final DocItem item) {
		if (item != null) {
			items.add(item);
		}
	}
	
	public List<DocItem> getItems() {
		return items;
	}
	
	public boolean isItemsEmpty() {
		return items.isEmpty();
	}
	
	
	private final String title;
	private final String id;
	private final List<String> headers = new ArrayList<>();
	private final List<String> footers = new ArrayList<>();
	
	private final List<DocSection> sections = new ArrayList<>();	
	private final List<DocItem> items = new ArrayList<>();
}
