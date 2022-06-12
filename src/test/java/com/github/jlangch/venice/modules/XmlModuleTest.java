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
package com.github.jlangch.venice.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.Parameters;
import com.github.jlangch.venice.Venice;


public class XmlModuleTest {

    @Test
    public void test_xml_1() {
        final Venice venice = new Venice();

        final String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<note type=\"private\">" +
                "  <to>Tove</to>" +
                "  <from>Jani</from>" +
                "  <heading>Reminder</heading>" +
                "  <body>Don't forget me this weekend!</body>" +
                "</note>";

        final String script =
                "(do                                                                                               \n" +
                "   (load-module :xml)                                                                             \n" +
                "                                                                                                  \n" +
                "   (let [data (xml/parse-str xml)]                                                                \n" +
                "      (assert (== \"private\" (-> data :attrs :type)))                                            \n" +
                "                                                                                                  \n" +
                "      (assert (== \"to\" (:tag (nth (:content data) 0))))                                         \n" +
                "      (assert (== nil (:attrs (nth (:content data) 0))))                                          \n" +
                "      (assert (== \"Tove\" (first (:content (nth (:content data) 0)))))                           \n" +
                "                                                                                                  \n" +
                "      (assert (== \"from\" (:tag (nth (:content data) 1))))                                       \n" +
                "      (assert (== nil (:attrs (nth (:content data) 1))))                                          \n" +
                "      (assert (== \"Jani\" (first (:content (nth (:content data) 1)))))                           \n" +
                "                                                                                                  \n" +
                "      (assert (== \"heading\" (:tag (nth (:content data) 2))))                                    \n" +
                "      (assert (== nil (:attrs (nth (:content data) 2))))                                          \n" +
                "      (assert (== \"Reminder\" (first (:content (nth (:content data) 2)))))                       \n" +
                "                                                                                                  \n" +
                "      (assert (== \"body\" (:tag (nth (:content data) 3))))                                       \n" +
                "      (assert (== nil (:attrs (nth (:content data) 3))))                                          \n" +
                "      (assert (== \"Don't forget me this weekend!\" (first (:content (nth (:content data) 3)))))  \n" +
                "                                                                                                  \n" +
                "      (str data))                                                                                 \n" +
                ") ";

        assertEquals(
            "{:attrs {:type private} " +
             ":content [{:content [Tove] :tag to} " +
                       "{:content [Jani] :tag from} " +
                       "{:content [Reminder] :tag heading} " +
                       "{:content [Don't forget me this weekend!] :tag body}] " +
             ":tag note}",
            venice.eval(script, Parameters.of("xml", xml)));
    }

    @Test
    public void test_xml_2() {
        final Venice venice = new Venice();

        final String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<a>" +
                "  <b>" +
                "    <c>" +
                "      <d>D</d>" +
                "    </c>" +
                "  </b>" +
                "</a>";

        final String script =
                "(do                                                                                                          \n" +
                "   (load-module :xml)                                                                                        \n" +
                "                                                                                                             \n" +
                "   (defn source [xml]                                                                                        \n" +
                "      (xml/input-source-from-str xml))                                                                       \n" +
                "                                                                                                             \n" +
                "   (let [data (xml/parse (source xml))]                                                                      \n" +
                "      (assert (== nil (:attrs data)))                                                                        \n" +
                "      (assert (== \"a\" (:tag data)))                                                                        \n" +
                "                                                                                                             \n" +
                "      (assert (== nil (:attrs (first (:content data)))))                                                     \n" +
                "      (assert (== \"b\" (:tag (first (:content data)))))                                                     \n" +
                "                                                                                                             \n" +
                "      (assert (== nil (:attrs (first (:content (first (:content data)))))))                                  \n" +
                "      (assert (== \"c\" (:tag (first (:content (first (:content data)))))))                                  \n" +
                "                                                                                                             \n" +
                "      (assert (== nil (:attrs (first (:content (first (:content (first (:content data)))))))))               \n" +
                "      (assert (== \"d\" (:tag (first (:content (first (:content (first (:content data)))))))))               \n" +
                "      (assert (== \"D\" (first (:content (first (:content (first (:content (first (:content data))))))))))   \n" +
                "                                                                                                             \n" +
                "      (str data))                                                                                            \n" +
                ") ";

        assertEquals(
            "{:content [{:content [{:content [{:content [D] " +
                                              ":tag d}] " +
                                   ":tag c}] " +
                        ":tag b}] " +
             ":tag a}",
            venice.eval(script, Parameters.of("xml", xml)));
    }
}
