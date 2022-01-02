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
package com.github.jlangch.venice.util.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface IVeniceServlet {

	void init(ServletConfig config) throws ServletException;

	void destroy();

	void doGet(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws ServletException, IOException;

	void doHead(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;
	
	void doPost(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;
	
	void doPut(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;
	
	void doDelete(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;
	
	void doOptions(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;
	
	void doTrace(HttpServletRequest req, HttpServletResponse resp, HttpServlet servle) throws ServletException, IOException;

	long getLastModified(HttpServletRequest req);
}
