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
package com.github.jlangch.venice.impl.docgen.cheatsheet.section;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class JavaInteropSection implements ISectionBuilder {

    public JavaInteropSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Java Interoperability", "javainterop");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection java = new DocSection("Java", "javainterop.java");
        all.addSection(java);
        java.addItem(diBuilder.getDocItem("."));
        java.addItem(diBuilder.getDocItem("import"));
        java.addItem(diBuilder.getDocItem("java-iterator-to-list"));
        java.addItem(diBuilder.getDocItem("java-enumeration-to-list"));
        java.addItem(diBuilder.getDocItem("java-unwrap-optional"));
        java.addItem(diBuilder.getDocItem("cast"));
        java.addItem(diBuilder.getDocItem("class"));

        final DocSection proxy = new DocSection("Proxify", "javainterop.proxify");
        all.addSection(proxy);
        proxy.addItem(diBuilder.getDocItem("proxify"));
        proxy.addItem(diBuilder.getDocItem("java/as-runnable"));
        proxy.addItem(diBuilder.getDocItem("java/as-callable"));
        proxy.addItem(diBuilder.getDocItem("java/as-predicate"));
        proxy.addItem(diBuilder.getDocItem("java/as-function"));
        proxy.addItem(diBuilder.getDocItem("java/as-consumer"));
        proxy.addItem(diBuilder.getDocItem("java/as-supplier"));
        proxy.addItem(diBuilder.getDocItem("java/as-bipredicate"));
        proxy.addItem(diBuilder.getDocItem("java/as-bifunction"));
        proxy.addItem(diBuilder.getDocItem("java/as-biconsumer"));
        proxy.addItem(diBuilder.getDocItem("java/as-unaryoperator"));
        proxy.addItem(diBuilder.getDocItem("java/as-binaryoperator"));

        final DocSection test = new DocSection("Test", "javainterop.test");
        all.addSection(test);
        test.addItem(diBuilder.getDocItem("java-obj?"));
        test.addItem(diBuilder.getDocItem("exists-class?"));
        test.addItem(diBuilder.getDocItem("enum?"));

        final DocSection clazz = new DocSection("Classes", "javainterop.classes");
        all.addSection(clazz);
        clazz.addItem(diBuilder.getDocItem("class"));
        clazz.addItem(diBuilder.getDocItem("class-of"));
        clazz.addItem(diBuilder.getDocItem("class-name"));
        clazz.addItem(diBuilder.getDocItem("class-version"));

        final DocSection types = new DocSection("Types", "javainterop.types");
        all.addSection(types);
        types.addItem(diBuilder.getDocItem("formal-type"));
        types.addItem(diBuilder.getDocItem("remove-formal-type"));
        types.addItem(diBuilder.getDocItem("class"));
        types.addItem(diBuilder.getDocItem("supers"));
        types.addItem(diBuilder.getDocItem("bases"));

        final DocSection support = new DocSection("Support", "javainterop.support");
        all.addSection(support);
        support.addItem(diBuilder.getDocItem("imports"));
        support.addItem(diBuilder.getDocItem("stacktrace", false, false));
        support.addItem(diBuilder.getDocItem("classloader"));
        support.addItem(diBuilder.getDocItem("classloader-of"));

        final DocSection jar = new DocSection("JARs", "javainterop.jar");
        all.addSection(jar);
        jar.addItem(diBuilder.getDocItem("jar-maven-manifest-version"));
        jar.addItem(diBuilder.getDocItem("java-package-version"));

        final DocSection modules = new DocSection("Modules", "javainterop.modules");
        all.addSection(modules);
        modules.addItem(diBuilder.getDocItem("module-name", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
