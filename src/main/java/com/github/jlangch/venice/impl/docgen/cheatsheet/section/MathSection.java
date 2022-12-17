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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class MathSection implements ISectionBuilder {

    public MathSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Math", "math");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection constants = new DocSection("Constants", "math.constants");
        section.addSection(constants);
        constants.addLiteralItem("E",  "math/E",  id());
        constants.addLiteralItem("PI", "math/PI", id());

        final DocSection arithmetic = new DocSection("Arithmetic", "math.arithmetic");
        all.addSection(arithmetic);
        arithmetic.addItem(diBuilder.getDocItem("inc"));
        arithmetic.addItem(diBuilder.getDocItem("dec"));
        arithmetic.addItem(diBuilder.getDocItem("min"));
        arithmetic.addItem(diBuilder.getDocItem("max"));
        arithmetic.addItem(diBuilder.getDocItem("mod"));
        arithmetic.addItem(diBuilder.getDocItem("mod-floor"));
        arithmetic.addItem(diBuilder.getDocItem("abs"));
        arithmetic.addItem(diBuilder.getDocItem("sgn"));
        arithmetic.addItem(diBuilder.getDocItem("negate"));
        arithmetic.addItem(diBuilder.getDocItem("floor"));
        arithmetic.addItem(diBuilder.getDocItem("ceil"));
        arithmetic.addItem(diBuilder.getDocItem("sqrt"));
        arithmetic.addItem(diBuilder.getDocItem("square"));
        arithmetic.addItem(diBuilder.getDocItem("pow"));
        arithmetic.addItem(diBuilder.getDocItem("exp"));
        arithmetic.addItem(diBuilder.getDocItem("log"));
        arithmetic.addItem(diBuilder.getDocItem("log10"));

        final DocSection util = new DocSection("Util", "math.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("digits"));

        final DocSection random = new DocSection("Random", "math.random");
        all.addSection(random);
        random.addItem(diBuilder.getDocItem("rand-long"));
        random.addItem(diBuilder.getDocItem("rand-double"));
        random.addItem(diBuilder.getDocItem("rand-gaussian"));

        final DocSection trigonometry = new DocSection("Trigonometry", "math.trigonometry");
        all.addSection(trigonometry);
        trigonometry.addItem(diBuilder.getDocItem("math/to-radians"));
        trigonometry.addItem(diBuilder.getDocItem("math/to-degrees"));
        trigonometry.addItem(diBuilder.getDocItem("math/sin"));
        trigonometry.addItem(diBuilder.getDocItem("math/cos"));
        trigonometry.addItem(diBuilder.getDocItem("math/tan"));
        trigonometry.addItem(diBuilder.getDocItem("math/asin"));
        trigonometry.addItem(diBuilder.getDocItem("math/acos"));
        trigonometry.addItem(diBuilder.getDocItem("math/atan"));

        final DocSection statistics = new DocSection("Statistics", "math.statistics");
        all.addSection(statistics);
        statistics.addItem(diBuilder.getDocItem("math/mean"));
        statistics.addItem(diBuilder.getDocItem("math/median"));
        statistics.addItem(diBuilder.getDocItem("math/quartiles"));
        statistics.addItem(diBuilder.getDocItem("math/quantile"));
        statistics.addItem(diBuilder.getDocItem("math/standard-deviation"));

        final DocSection algo = new DocSection("Algorithms", "math.algo");
        all.addSection(algo);
        algo.addItem(diBuilder.getDocItem("math/softmax"));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
