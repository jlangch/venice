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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public interface IVeniceServlet {

    default void init(final ServletConfig config) throws ServletException {
    }

    default void destroy(final HttpServlet servlet) {
    }

    default void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "GET");
    }

    default void doHead(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "HEAD");
    }

    default void doPost(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "POST");
    }

    default void doPut(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "PUT");
    }

    default void doDelete(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "DELETE");
    }

    default void doOptions(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "OPTIONS");
    }

    default void doTrace(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotImplemented(resp, "TRACE");
    }

    default long getLastModified(final HttpServletRequest req) {
        return -1;
    }


    static void sendNotImplemented(
    		final HttpServletResponse resp,
    		final String method
    ) throws IOException {
        resp.setStatus(501);
        resp.setContentType("text/html");
        resp.getWriter().println(
        		  "<html>\n"
        		+ "  <head>\n"
        		+ "    <style>\n"
        		+ "      body    {font-family: Arial, Helvetica, sans-serif;}\n"
        		+ "      .box    {margin-top: 120px; padding: 100px; text-align: center; background-color: #f8f8f8;}\n"
        		+ "      .title  {color: #cccccc; font-size: 90px;}\n"
        		+ "      .msg    {margin-top: 20px; color: #999999; font-size: 36px;}\n"
        		+ "    </style>\n"
        		+ "  </head>\n"
        		+ "  <body>\n"
        		+ "    <div class=\"box\">\n"
        		+ "      <div class=\"title\">Not Implemented</div>\n"
        		+ "      <div class=\"msg\">HTTP Method "+ method + "</div>\n"
        		+ "    </div>\n"
        		+ "  </body>\n"
        		+ "</html>\n");
    }
}
