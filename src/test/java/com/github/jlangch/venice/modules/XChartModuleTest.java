/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2018 Venice
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
package com.github.jlangch.venice.modules;

import org.junit.Test;

import com.github.jlangch.venice.Venice;


public class XChartModuleTest {

	@Test
	public void test_xy_chart() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                   \n" +
			"	(load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"	(xchart/swing-view-chart                                           \n" +
			"	   (xchart/xy-chart                                                \n" +
			"	       { \"Maxime\" { :x (range 10)                                \n" +
			"	                      :y (mapv (fn [x] (+ x (rand-double 3.0)))    \n" +
			"	                             (range 10)) }                         \n" +
			"            \"Tyrone\" { :x (range 10)                                \n" +
			"	                      :y (mapv (fn [x] (+ 2 x (rand-double 4.0)))  \n" +
			"	                            (range 0 5 0.5)) }}                    \n" +
			"                                                                      \n" +
			"	      { :title \"Longest running distance\"                        \n" +
			"	        :x-axis { :title \"Months (since start)\" }                \n" +
			"	        :y-axis { :title \"Distance\"                              \n" +
			"	                  :decimal-pattern \"##.## km\" }                  \n" +
			"	        :theme :matlab } ))                                        \n" +
			"	                                                                   \n" +
			"	 (sleep 20000)                                                     \n" +
			") ";
		
		System.out.println(venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_pie_chart() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                   \n" +
			"	(load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"	(xchart/swing-view-chart                                           \n" +
			"	   (xchart/pie-chart                                               \n" +
			"	       { \"Spaces\" 400                                            \n" + 
			"            \"Tabs\" 310                                              \n" + 
			"            \"A mix of both\" 50 }))                                  \n" +
			"	                                                                   \n" +
			"	 (sleep 20000)                                                     \n" +
			") ";
		
		System.out.println(venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_pie_chart_donut() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                   \n" +
			"	(load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"	(xchart/swing-view-chart                                           \n" +
			"	   (xchart/pie-chart                                               \n" +
			"	       { \":none\" 845                                             \n" + 
			"            \":simple\" 371                                           \n" + 
			"            \":whitespace\" 303                                       \n" +
			"            \":advanced\" 1013 }                                      \n" +
			"	       { :title (str \"Which ClojureScript optimization \"         \n" + 
			"			             \"settings do you use?\")                     \n" + 
			"            :render-style :donut                                      \n" +
			"            :annotation-distance 0.82 }))                             \n" +
			"	                                                                   \n" +
			"	 (sleep 20000)                                                     \n" +
			") ";
		
		System.out.println(venice.eval("(str " + script + ")"));
	}

}
