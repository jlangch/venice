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
			"   (load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"   (xchart/swing-view-chart                                           \n" +
			"      (xchart/xy-chart                                                \n" +
			"          { \"Maxime\" { :x (range 10)                                \n" +
			"                         :y (mapv (fn [x] (+ x (rand-double 3.0)))    \n" +
			"                                (range 10)) }                         \n" +
			"            \"Tyrone\" { :x (range 10)                                \n" +
			"                         :y (mapv (fn [x] (+ 2 x (rand-double 4.0)))  \n" +
			"                               (range 0 5 0.5)) }}                    \n" +
			"                                                                      \n" +
			"         { :title \"Longest running distance\"                        \n" +
			"           :x-axis { :title \"Months (since start)\" }                \n" +
			"           :y-axis { :title \"Distance\"                              \n" +
			"                     :decimal-pattern \"##.## km\" }                  \n" +
			"           :theme :matlab } ))                                        \n" +
			"                                                                      \n" +
			"    (sleep 20000)                                                     \n" +
			") ";
		
		System.out.println(venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_category_chart() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                               \n" +
			"   (load-module :xchart)                                                          \n" +
			"                                                                                  \n" +
			"   (xchart/swing-view-chart                                                       \n" +
			"      (xchart/category-chart                                                      \n" +
			"          {\"Bananas\" {\"Mon\" 6, \"Tue\" 2, \"Fri\" 3, \"Wed\" 1, \"Thur\" 3}   \n" +
			"           \"Apples\" {\"Tue\" 3, \"Wed\" 5, \"Fri\" 1, \"Mon\" 1}                \n" +
			"           \"Pears\" {\"Thur\" 1, \"Mon\" 3, \"Fri\" 4, \"Wed\" 1}}               \n" +
			"          {:title \"Weekly Fruit Sales\"                                          \n" +
			"           :theme :ggplot2                                                        \n" +
			"           :x-axis {:order [\"Mon\" \"Tue\" \"Wed\" \"Thur\" \"Fri\"]}} ))        \n" +
			"                                                                                  \n" +
			"    (sleep 20000)                                                                 \n" +
			") ";
		  
		  
		System.out.println(venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_pie_chart() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                   \n" +
			"   (load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"   (xchart/swing-view-chart                                           \n" +
			"      (xchart/pie-chart                                               \n" +
			"          { \"Spaces\" 400                                            \n" +
			"            \"Tabs\" 310                                              \n" +
			"            \"A mix of both\" 50 }))                                  \n" +
			"                                                                      \n" +
			"    (sleep 20000)                                                     \n" +
			") ";
		
		System.out.println(venice.eval("(str " + script + ")"));
	}

	@Test
	public void test_bubble_chart() {
		final Venice venice = new Venice();

		final String script =
			"(do                                                                   \n" +
			"   (load-module :xchart)                                              \n" +
			"                                                                      \n" +
			"   (def series1                                                       \n" +
			"        [ {:x  1 :y  2 :bubble 10}                                    \n" +
			"          {:x 10 :y  8 :bubble  4}                                    \n" +
			"          {:x 20 :y 25 :bubble  8} ])                                 \n" +
			"                                                                      \n" +
			"   (def series2                                                       \n" +
			"        [ {:x 10 :y  4 :bubble 10}                                    \n" +
			"          {:x  5 :y  5 :bubble 12}                                    \n" +
			"          {:x 18 :y 20 :bubble  3} ])                                 \n" +
			"                                                                      \n" +			
			"   (def bubblify                                                      \n" +
			"      (fn [series]                                                    \n" +
			"          {:x (map (fn [t] (:x t)) series)                            \n" +
			"           :y (map (fn [t] (:y t)) series)                            \n" +
			"           :bubble (map (fn [t] (:bubble t)) series)}))               \n" +
			"                                                                      \n" +			
			"   (xchart/swing-view-chart                                           \n" +
			"      (xchart/bubble-chart                                            \n" +
			"          {\"Series 1\" (bubblify series1)                            \n" +
			"           \"Series 2\" (bubblify series2) }                          \n" +
			"          {:title \"Bubble Chart\"                                    \n" +
			"           :legend {:position :inside-ne}                             \n" +
			"           :y-axis {:title \"Series 1\"}                              \n" +
			"           :x-axis {:title \"Series 2\" :logarithmic? false}}))       \n" +
			"                                                                      \n" +
			"    (sleep 20000)                                                     \n" +
			") ";

		System.out.println(venice.eval("(str " + script + ")"));
	}

}
