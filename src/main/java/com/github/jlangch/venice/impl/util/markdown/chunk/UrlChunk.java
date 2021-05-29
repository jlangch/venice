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
package com.github.jlangch.venice.impl.util.markdown.chunk;

import com.github.jlangch.venice.impl.util.StringUtil;

public class UrlChunk implements Chunk {

	public UrlChunk() {
		this("", "");
	}

	public UrlChunk(final String caption, final String url) {
		this.caption = StringUtil.trimToEmpty(caption);
		this.url = StringUtil.trimToEmpty(url);
	}


	@Override
	public boolean isEmpty() {
		return url.isEmpty();
	}
	
	public String getCaption() {
		return caption;
	}
	
	public String getUrl() {
		return url;
	}

	
	private final String caption;
	private final String url;
}
