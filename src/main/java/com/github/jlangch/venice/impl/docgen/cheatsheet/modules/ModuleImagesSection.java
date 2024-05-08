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
package com.github.jlangch.venice.impl.docgen.cheatsheet.modules;

import com.github.jlangch.venice.impl.docgen.cheatsheet.DocItemBuilder;
import com.github.jlangch.venice.impl.docgen.cheatsheet.DocSection;
import com.github.jlangch.venice.impl.docgen.cheatsheet.ISectionBuilder;


public class ModuleImagesSection implements ISectionBuilder {

    public ModuleImagesSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection(
                                        "Images",
                                        "modules.images");

        final DocSection all = new DocSection("(load-module :images)", id());
        section.addSection(all);

        final DocSection load = new DocSection("Load/Save", id());
        all.addSection(load);
        load.addItem(diBuilder.getDocItem("images/load", false));
        load.addItem(diBuilder.getDocItem("images/save", false));

        final DocSection create = new DocSection("Create/Copy", id());
        all.addSection(create);
        create.addItem(diBuilder.getDocItem("images/create", false));
        create.addItem(diBuilder.getDocItem("images/copy", false));

        final DocSection props = new DocSection("Properties", id());
        all.addSection(props);
        props.addItem(diBuilder.getDocItem("images/dimension", false));

        final DocSection formats = new DocSection("File Formats", id());
        all.addSection(formats);
        formats.addItem(diBuilder.getDocItem("images/format-names", true));

        final DocSection transform = new DocSection("Transform", id());
        all.addSection(transform);
        transform.addItem(diBuilder.getDocItem("images/rotate", false));
        transform.addItem(diBuilder.getDocItem("images/flip", false));
        transform.addItem(diBuilder.getDocItem("images/crop", false));
        transform.addItem(diBuilder.getDocItem("images/pad", false));
        transform.addItem(diBuilder.getDocItem("images/resize-fit", false));
        transform.addItem(diBuilder.getDocItem("images/resize", false));
        transform.addItem(diBuilder.getDocItem("images/shear", false));
        transform.addItem(diBuilder.getDocItem("images/translate", false));
        transform.addItem(diBuilder.getDocItem("images/apply-ops", false));

        final DocSection g2d = new DocSection("G2D", id());
        all.addSection(g2d);
        g2d.addItem(diBuilder.getDocItem("images/g2d", false));
        g2d.addItem(diBuilder.getDocItem("images/anti-alias", false));
        g2d.addItem(diBuilder.getDocItem("images/stroke", false));
        g2d.addItem(diBuilder.getDocItem("images/fg-color", false));
        g2d.addItem(diBuilder.getDocItem("images/bg-color", false));
        g2d.addItem(diBuilder.getDocItem("images/get-clip", false));
        g2d.addItem(diBuilder.getDocItem("images/set-clip", false));
        g2d.addItem(diBuilder.getDocItem("images/get-clip-bounds", false));

        final DocSection drawing = new DocSection("Drawing", id());
        all.addSection(drawing);
        drawing.addItem(diBuilder.getDocItem("images/copy-area", false));
        drawing.addItem(diBuilder.getDocItem("images/clear-rect", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-circle", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-oval", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-rect", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-round-rect", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-string", false));
        drawing.addItem(diBuilder.getDocItem("images/draw-line", false));

        final DocSection fill = new DocSection("Filling", id());
        all.addSection(fill);
        fill.addItem(diBuilder.getDocItem("images/fill-circle", false));
        fill.addItem(diBuilder.getDocItem("images/fill-oval", false));
        fill.addItem(diBuilder.getDocItem("images/fill-rect", false));
        fill.addItem(diBuilder.getDocItem("images/fill-round-rect", false));

        final DocSection shapes = new DocSection("Shapes", id());
        all.addSection(shapes);
        shapes.addItem(diBuilder.getDocItem("images/point", false));
        shapes.addItem(diBuilder.getDocItem("images/rectangle", false));
        shapes.addItem(diBuilder.getDocItem("images/polygon", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
