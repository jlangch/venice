/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
package com.github.jlangch.venice.impl.docgen;

import java.util.ArrayList;
import java.util.List;


public class DocSection {
	
	public DocSection(final String title, final String id) {
		this.title = title;
		this.id = id;
	}
	
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
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
	
	private final List<DocSection> sections = new ArrayList<>();	
	private final List<DocItem> items = new ArrayList<>();
}
