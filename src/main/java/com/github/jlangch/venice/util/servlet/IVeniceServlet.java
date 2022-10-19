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

    default void init(final ServletConfig config) throws ServletException {
    }

    default void destroy() {
    }

    default void doGet(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doHead(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doPost(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doPut(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doDelete(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doOptions(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default void doTrace(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final HttpServlet servlet
    ) throws ServletException, IOException {
        sendNotFound(resp);
    }

    default long getLastModified(final HttpServletRequest req) {
        return -1;
    }


    static void sendNotFound(final HttpServletResponse resp) throws IOException {
        resp.setStatus(404);
        resp.setContentType("text/html");
        resp.getWriter().println(
                "<html>" +
                "  <body>" +
                "    <div style=\"text-align: center; margin-top: 120px; background-color: #f8f8f8; padding: 100px;\">" +
                "      <div style=\"font-family: Arial, Helvetica, sans-serif; color: #CCCCCC; font-size: 90px;\">" +
                "        Not Found" +
                "      </div>" +
                "      <div style=\"font-family: Arial, Helvetica, sans-serif; color: #999999; font-size: 36px; margin-top: 20px;\">" +
                "        the requested webpage was not found" +
                "      </div>" +
                "    </div>" +
                "  </body>" +
                "</html>");
    }
}
