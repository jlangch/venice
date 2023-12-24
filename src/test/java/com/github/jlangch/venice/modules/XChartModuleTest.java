/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2024 Venice
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Venice;


@Disabled
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
            "                     :decimal-pattern \"##.## km\"                    \n" +
            "                     :label { :alignment :right} }                    \n" +
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
            "           :theme :xchart                                                         \n" +
            "           :x-axis {:order [\"Mon\" \"Tue\" \"Wed\" \"Thur\" \"Fri\"]}} ))        \n" +
            "                                                                                  \n" +
            "    (sleep 20000)                                                                 \n" +
            ") ";


        System.out.println(venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_category_chart_overlapping() {
        final Venice venice = new Venice();

        final String script =
            "(do                                                                               \n" +
            "   (load-module :xchart)                                                          \n" +
            "                                                                                  \n" +
            "   (def my-red (. :java.awt.Color :getHSBColor 0.0 0.8 0.9))                      \n" +
            "   (def my-darker-red (. my-red :darker ))                                        \n" +
            "                                                                                  \n" +
            "   (xchart/swing-view-chart                                                       \n" +
            "      (xchart/category-chart                                                      \n" +
            "          {\"A\" {:x [14 15 16 17 18 19 20 21 22]                                 \n" +
            "                   :y [2.03 7.39 17.20 25.66 24.55                                \n" +
            "                       15.05 5.92 1.49 0.24]                                      \n" +
            "                   :style {:fill-color my-red}}                                   \n" +
            "           \"B\" {:x [14 15 16 17 18 19 20 21 22]                                 \n" +
            "                   :y [0.01 0.03 0.67 5.54 16.66                                  \n" +
            "                       14.41 5.48 1.11 0.06]                                      \n" +
            "                   :style {:fill-color my-darker-red}}}                           \n" +
            "          {:title \"Store sales on Monday\"                                       \n" +
            "           :overlap? true                                                         \n" +
            "           :theme :xchart                                                         \n" +
            "           :series-order [\"A\" \"B\"]                                            \n" +
            "           :y-axis {:ticks-visible? false}                                        \n" +
            "           :x-axis {:decimal-pattern \"##.00\"}}))                                \n" +
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

    @Test
    public void test_sticks_chart() {
        final Venice venice = new Venice();

        final String script =
            "(do                                                                   \n" +
            "   (load-module :xchart)                                              \n" +
            "                                                                      \n" +
            "   (xchart/swing-view-chart                                           \n" +
            "      (xchart/category-chart                                          \n" +
            "         (xchart/transpose-map                                        \n" +
            "          {\"Group 1\" {\"A\" 1329,                                   \n" +
            "                        \"B\" 47,                                     \n" +
            "                        \"C\" 830}                                    \n" +
            "           \"Group 2\" {\"A\" 1049,                                   \n" +
            "                        \"B\" 32,                                     \n" +
            "                        \"C\" 1015}                                   \n" +
            "           \"Group 3\" {\"A\" 435,                                    \n" +
            "                        \"B\" 295,                                    \n" +
            "                        \"C\" 1463}                                   \n" +
            "           \"Group 4\" {\"A\" 1221,                                   \n" +
            "                        \"B\" 36,                                     \n" +
            "                        \"C\" 910}})                                  \n" +
            "          {:title \"Stick Chart\"                                     \n" +
            "           :render-style :stick                                       \n" +
            "           :y-axis {:ticks-visible? false}                            \n" +
            "           :x-axis {:label {:rotation 0}}}))                          \n" +
            "                                                                      \n" +
            "    (sleep 20000)                                                     \n" +
            ") ";

        System.out.println(venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_misc_chart() {
        final Venice venice = new Venice();

        final String script =
            "(do                                                                             \n" +
            "   (import :java.lang.Math)                                                     \n" +
            "                                                                                \n" +
            "   (load-module :xchart)                                                        \n" +
            "                                                                                \n" +
            "   (def log-spiral-x (fn [a b t]                                                \n" +
            "        (* a (. :Math :exp (* b t)) (. :Math :cos t))))                         \n" +
            "                                                                                \n" +
            "   (def log-spiral-y (fn [a b t]                                                \n" +
            "        (* a (. :Math :exp (* b t)) (. :Math :sin t))))                         \n" +
            "                                                                                \n" +
            "   (xchart/swing-view-chart                                                     \n" +
            "      (xchart/xy-chart                                                          \n" +
            "          {\"curve\" {:x (cons 0 (map (fn [x] (+ 2 (log-spiral-x -0.2 0.2 x)))  \n" +
            "                                 (range 10.5 0 -0.1)))                          \n" +
            "                      :y (cons 0 (map (fn [x] (+ 4 (log-spiral-y 0.2 0.2 x)))   \n" +
            "                                 (range 10.5 0 -0.1)))                          \n" +
            "                      :style {:marker-type :none}}}                             \n" +
            "          {:title \"Spiral\"                                                    \n" +
            "           :legend {:visible? false}                                            \n" +
            "           :axis {:ticks {:visible? false}}}))                                  \n" +
            "                                                                                \n" +
            "    (sleep 20000)                                                               \n" +
            ") ";

        System.out.println(venice.eval("(str " + script + ")"));
    }

    @Test
    public void test_encoder() {
        final Venice venice = new Venice();

        final String script =
            "(do                                                  \n" +
            "   (load-module :xchart)                             \n" +
            "                                                     \n" +
            "   (xchart/to-bytes-with-dpi-ex                      \n" +
            "      (xchart/pie-chart                              \n" +
            "         { \"A\" 400 \"B\" 310 \"C\" 50 }            \n" +
            "         { :title \"Pie Chart\" :theme :xchart } )   \n" +
            "      :png                                           \n" +
            "      120)                                           \n" +
            ") ";

        System.out.println(venice.eval(script));
    }

    @Test
    public void test_doto_cond() {
        final Venice venice = new Venice();

        final String script =
            "(do                                                                \n" +
            "   (load-module :xchart)                                           \n" +
            "                                                                   \n" +
            "   (def p (fn [x y] (println (str x \" : \" y))))                  \n" +
            "                                                                   \n" +
            "   (p \"...test\" \"start\")                                       \n" +
            "                                                                   \n" +
            "   (xchart/doto-cond                                               \n" +
            "      100                                                          \n" +
            "      200 (p 200)                                                  \n" +
            "      300 (p 300))                                                 \n" +
            "                                                                   \n" +
            "   (macroexpand '(xchart/doto-cond 100 200 (p 200) 300 (p 300)))   \n" +
            ") ";

        System.out.println(venice.eval("(str " + script + ")"));
    }

}
