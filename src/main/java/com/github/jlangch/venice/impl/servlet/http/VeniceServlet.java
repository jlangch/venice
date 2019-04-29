/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl.servlet.http;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class VeniceServlet extends HttpServlet {

	public VeniceServlet(final IVeniceServlet delegate) { 
		this.delegate = delegate;
	}

	
	@Override
	public void init(final ServletConfig config)
	throws ServletException {
		super.init(config);
		delegate.init(config);
	}

	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}

	@Override
	protected void doGet(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doGet(req, resp);
	}

	@Override
	protected void doHead(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doHead(req, resp);
	}
	
	@Override
	protected void doPost(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doPost(req, resp);
	}
	
	@Override
	protected void doPut(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doPut(req, resp);
	}
	
	@Override
	protected void doDelete(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doDelete(req, resp);
	}
	
	@Override
	protected void doOptions(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doOptions(req, resp);
	}
	
	@Override
	protected void doTrace(
			final HttpServletRequest req, 
			final HttpServletResponse resp
	) throws ServletException, IOException {
		delegate.doTrace(req, resp);
	}

	@Override
	protected long getLastModified(final HttpServletRequest req) {
		return delegate.getLastModified(req);
	}


	private static final long serialVersionUID = 7024848763477707717L;
	
	private final IVeniceServlet delegate;
}
