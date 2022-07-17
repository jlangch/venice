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


public class CollectionsSection implements ISectionBuilder {

    public CollectionsSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("Collections", "collections");


        final DocSection collections = new DocSection("Collections", "collections.collections");
        section.addSection(collections);

        final DocSection generic = new DocSection("Generic", "collections.collections.generic");
        collections.addSection(generic);
        generic.addItem(diBuilder.getDocItem("count"));
        generic.addItem(diBuilder.getDocItem("compare"));
        generic.addItem(diBuilder.getDocItem("empty-to-nil"));
        generic.addItem(diBuilder.getDocItem("empty"));
        generic.addItem(diBuilder.getDocItem("into"));
        generic.addItem(diBuilder.getDocItem("into!"));
        generic.addItem(diBuilder.getDocItem("cons"));
        generic.addItem(diBuilder.getDocItem("conj"));
        generic.addItem(diBuilder.getDocItem("conj!"));
        generic.addItem(diBuilder.getDocItem("remove"));
        generic.addItem(diBuilder.getDocItem("repeat"));
        generic.addItem(diBuilder.getDocItem("repeatedly"));
        generic.addItem(diBuilder.getDocItem("cycle"));
        generic.addItem(diBuilder.getDocItem("replace"));
        generic.addItem(diBuilder.getDocItem("range"));
        generic.addItem(diBuilder.getDocItem("group-by"));
        generic.addItem(diBuilder.getDocItem("frequencies"));
        generic.addItem(diBuilder.getDocItem("get-in"));
        generic.addItem(diBuilder.getDocItem("seq"));
        generic.addItem(diBuilder.getDocItem("reverse"));
        generic.addItem(diBuilder.getDocItem("shuffle"));

        final DocSection coll_test = new DocSection("Tests", "collections.collections.tests");
        collections.addSection(coll_test);
        coll_test.addItem(diBuilder.getDocItem("empty?"));
        coll_test.addItem(diBuilder.getDocItem("not-empty?"));
        coll_test.addItem(diBuilder.getDocItem("coll?"));
        coll_test.addItem(diBuilder.getDocItem("list?"));
        coll_test.addItem(diBuilder.getDocItem("vector?"));
        coll_test.addItem(diBuilder.getDocItem("set?"));
        coll_test.addItem(diBuilder.getDocItem("sorted-set?"));
        coll_test.addItem(diBuilder.getDocItem("mutable-set?"));
        coll_test.addItem(diBuilder.getDocItem("map?"));
        coll_test.addItem(diBuilder.getDocItem("sequential?"));
        coll_test.addItem(diBuilder.getDocItem("hash-map?"));
        coll_test.addItem(diBuilder.getDocItem("ordered-map?"));
        coll_test.addItem(diBuilder.getDocItem("sorted-map?"));
        coll_test.addItem(diBuilder.getDocItem("mutable-map?"));
        coll_test.addItem(diBuilder.getDocItem("bytebuf?"));

        final DocSection coll_process = new DocSection("Process", "collections.collections.process");
        collections.addSection(coll_process);
        coll_process.addItem(diBuilder.getDocItem("map"));
        coll_process.addItem(diBuilder.getDocItem("map-indexed"));
        coll_process.addItem(diBuilder.getDocItem("filter"));
        coll_process.addItem(diBuilder.getDocItem("reduce"));
        coll_process.addItem(diBuilder.getDocItem("keep"));
        coll_process.addItem(diBuilder.getDocItem("docoll"));


        final DocSection lists = new DocSection("Lists", "collections.lists");
        section.addSection(lists);

        final DocSection list_create = new DocSection("Create", "collections.lists.create");
        lists.addSection(list_create);
        list_create.addItem(diBuilder.getDocItem("()"));
        list_create.addItem(diBuilder.getDocItem("list"));
        list_create.addItem(diBuilder.getDocItem("list*"));
        list_create.addItem(diBuilder.getDocItem("mutable-list"));

        final DocSection list_access = new DocSection("Access", "collections.lists.access");
        lists.addSection(list_access);
        list_access.addItem(diBuilder.getDocItem("first"));
        list_access.addItem(diBuilder.getDocItem("second"));
        list_access.addItem(diBuilder.getDocItem("third"));
        list_access.addItem(diBuilder.getDocItem("fourth"));
        list_access.addItem(diBuilder.getDocItem("nth"));
        list_access.addItem(diBuilder.getDocItem("last"));
        list_access.addItem(diBuilder.getDocItem("peek"));
        list_access.addItem(diBuilder.getDocItem("rest"));
        list_access.addItem(diBuilder.getDocItem("butlast"));
        list_access.addItem(diBuilder.getDocItem("nfirst"));
        list_access.addItem(diBuilder.getDocItem("nlast"));
        list_access.addItem(diBuilder.getDocItem("sublist"));
        list_access.addItem(diBuilder.getDocItem("some"));

        final DocSection list_modify = new DocSection("Modify", "collections.lists.modify");
        lists.addSection(list_modify);
        list_modify.addItem(diBuilder.getDocItem("cons"));
        list_modify.addItem(diBuilder.getDocItem("conj"));
        list_modify.addItem(diBuilder.getDocItem("conj!"));
        list_modify.addItem(diBuilder.getDocItem("rest"));
        list_modify.addItem(diBuilder.getDocItem("pop"));
        list_modify.addItem(diBuilder.getDocItem("into"));
        list_modify.addItem(diBuilder.getDocItem("into!"));
        list_modify.addItem(diBuilder.getDocItem("concat"));
        list_modify.addItem(diBuilder.getDocItem("distinct"));
        list_modify.addItem(diBuilder.getDocItem("dedupe"));
        list_modify.addItem(diBuilder.getDocItem("partition"));
        list_modify.addItem(diBuilder.getDocItem("partition-by"));
        list_modify.addItem(diBuilder.getDocItem("interpose"));
        list_modify.addItem(diBuilder.getDocItem("interleave"));
        list_modify.addItem(diBuilder.getDocItem("cartesian-product"));
        list_modify.addItem(diBuilder.getDocItem("combinations"));
        list_modify.addItem(diBuilder.getDocItem("mapcat"));
        list_modify.addItem(diBuilder.getDocItem("flatten"));
        list_modify.addItem(diBuilder.getDocItem("sort"));
        list_modify.addItem(diBuilder.getDocItem("sort-by"));
        list_modify.addItem(diBuilder.getDocItem("take"));
        list_modify.addItem(diBuilder.getDocItem("take-while"));
        list_modify.addItem(diBuilder.getDocItem("take-last"));
        list_modify.addItem(diBuilder.getDocItem("drop"));
        list_modify.addItem(diBuilder.getDocItem("drop-while"));
        list_modify.addItem(diBuilder.getDocItem("drop-last"));
        list_modify.addItem(diBuilder.getDocItem("split-at"));
        list_modify.addItem(diBuilder.getDocItem("split-with"));

        final DocSection list_test = new DocSection("Test", "collections.lists.test");
        lists.addSection(list_test);
        list_test.addItem(diBuilder.getDocItem("list?"));
        list_test.addItem(diBuilder.getDocItem("mutable-list?"));
        list_test.addItem(diBuilder.getDocItem("every?"));
        list_test.addItem(diBuilder.getDocItem("not-every?"));
        list_test.addItem(diBuilder.getDocItem("any?"));
        list_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection vectors = new DocSection("Vectors", "collections.vectors");
        section.addSection(vectors);

        final DocSection vec_create = new DocSection("Create", "collections.vectors.create");
        vectors.addSection(vec_create);
        vec_create.addItem(diBuilder.getDocItem("[]"));
        vec_create.addItem(diBuilder.getDocItem("vector"));
        vec_create.addItem(diBuilder.getDocItem("vector*"));
        vec_create.addItem(diBuilder.getDocItem("mutable-vector"));
        vec_create.addItem(diBuilder.getDocItem("mapv"));

        final DocSection vec_access = new DocSection("Access", "collections.vectors.access");
        vectors.addSection(vec_access);
        vec_access.addItem(diBuilder.getDocItem("first"));
        vec_access.addItem(diBuilder.getDocItem("second"));
        vec_access.addItem(diBuilder.getDocItem("third"));
        vec_access.addItem(diBuilder.getDocItem("nth"));
        vec_access.addItem(diBuilder.getDocItem("last"));
        vec_access.addItem(diBuilder.getDocItem("peek"));
        vec_access.addItem(diBuilder.getDocItem("butlast"));
        vec_access.addItem(diBuilder.getDocItem("rest"));
        vec_access.addItem(diBuilder.getDocItem("nfirst"));
        vec_access.addItem(diBuilder.getDocItem("nlast"));
        vec_access.addItem(diBuilder.getDocItem("subvec"));
        vec_access.addItem(diBuilder.getDocItem("some"));

        final DocSection vec_modify = new DocSection("Modify", "collections.vectors.modify");
        vectors.addSection(vec_modify);
        vec_modify.addItem(diBuilder.getDocItem("cons"));
        vec_modify.addItem(diBuilder.getDocItem("conj"));
        vec_modify.addItem(diBuilder.getDocItem("conj!"));
        vec_modify.addItem(diBuilder.getDocItem("rest"));
        vec_modify.addItem(diBuilder.getDocItem("pop"));
        vec_modify.addItem(diBuilder.getDocItem("into"));
        vec_modify.addItem(diBuilder.getDocItem("into!"));
        vec_modify.addItem(diBuilder.getDocItem("concat"));
        vec_modify.addItem(diBuilder.getDocItem("distinct"));
        vec_modify.addItem(diBuilder.getDocItem("dedupe"));
        vec_modify.addItem(diBuilder.getDocItem("partition"));
        vec_modify.addItem(diBuilder.getDocItem("partition-by"));
        vec_modify.addItem(diBuilder.getDocItem("interpose"));
        vec_modify.addItem(diBuilder.getDocItem("interleave"));
        vec_modify.addItem(diBuilder.getDocItem("cartesian-product"));
        vec_modify.addItem(diBuilder.getDocItem("combinations"));
        vec_modify.addItem(diBuilder.getDocItem("mapcat"));
        vec_modify.addItem(diBuilder.getDocItem("flatten"));
        vec_modify.addItem(diBuilder.getDocItem("sort"));
        vec_modify.addItem(diBuilder.getDocItem("sort-by"));
        vec_modify.addItem(diBuilder.getDocItem("take"));
        vec_modify.addItem(diBuilder.getDocItem("take-while"));
        vec_modify.addItem(diBuilder.getDocItem("take-last"));
        vec_modify.addItem(diBuilder.getDocItem("drop"));
        vec_modify.addItem(diBuilder.getDocItem("drop-while"));
        vec_modify.addItem(diBuilder.getDocItem("drop-last"));
        vec_modify.addItem(diBuilder.getDocItem("update"));
        vec_modify.addItem(diBuilder.getDocItem("update!"));
        vec_modify.addItem(diBuilder.getDocItem("assoc"));
        vec_modify.addItem(diBuilder.getDocItem("assoc!"));
        vec_modify.addItem(diBuilder.getDocItem("split-with"));

        final DocSection vec_nested = new DocSection("Nested", "collections.vectors.nested");
        vectors.addSection(vec_nested);
        vec_nested.addItem(diBuilder.getDocItem("get-in"));
        vec_nested.addItem(diBuilder.getDocItem("assoc-in"));
        vec_nested.addItem(diBuilder.getDocItem("update-in"));
        vec_nested.addItem(diBuilder.getDocItem("dissoc-in"));

        final DocSection vec_test = new DocSection("Test", "collections.vectors.test");
        vectors.addSection(vec_test);
        vec_test.addItem(diBuilder.getDocItem("vector?"));
        vec_test.addItem(diBuilder.getDocItem("mutable-vector?"));
        vec_test.addItem(diBuilder.getDocItem("contains?"));
        vec_test.addItem(diBuilder.getDocItem("not-contains?"));
        vec_test.addItem(diBuilder.getDocItem("every?"));
        vec_test.addItem(diBuilder.getDocItem("not-every?"));
        vec_test.addItem(diBuilder.getDocItem("any?"));
        vec_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection sets = new DocSection("Sets", "collections.sets");
        section.addSection(sets);

        final DocSection set_create = new DocSection("Create", "collections.sets.create");
        sets.addSection(set_create);
        set_create.addItem(diBuilder.getDocItem("#{}"));
        set_create.addItem(diBuilder.getDocItem("set"));
        set_create.addItem(diBuilder.getDocItem("sorted-set"));
        set_create.addItem(diBuilder.getDocItem("mutable-set"));

        final DocSection set_modify = new DocSection("Modify", "collections.sets.modify");
        sets.addSection(set_modify);
        set_modify.addItem(diBuilder.getDocItem("into"));
        set_modify.addItem(diBuilder.getDocItem("into!"));
        set_modify.addItem(diBuilder.getDocItem("cons"));
        set_modify.addItem(diBuilder.getDocItem("cons!"));
        set_modify.addItem(diBuilder.getDocItem("conj"));
        set_modify.addItem(diBuilder.getDocItem("conj!"));
        set_modify.addItem(diBuilder.getDocItem("disj"));

        final DocSection algebra = new DocSection("Algebra", "collections.sets.algebra");
        sets.addSection(algebra);
        algebra.addItem(diBuilder.getDocItem("difference"));
        algebra.addItem(diBuilder.getDocItem("union"));
        algebra.addItem(diBuilder.getDocItem("intersection"));
        algebra.addItem(diBuilder.getDocItem("subset?"));
        algebra.addItem(diBuilder.getDocItem("superset?"));

        final DocSection set_test = new DocSection("Test", "collections.sets.test");
        sets.addSection(set_test);
        set_test.addItem(diBuilder.getDocItem("set?"));
        set_test.addItem(diBuilder.getDocItem("sorted-set?"));
        set_test.addItem(diBuilder.getDocItem("mutable-set?"));
        set_test.addItem(diBuilder.getDocItem("contains?"));
        set_test.addItem(diBuilder.getDocItem("not-contains?"));
        set_test.addItem(diBuilder.getDocItem("every?"));
        set_test.addItem(diBuilder.getDocItem("not-every?"));
        set_test.addItem(diBuilder.getDocItem("any?"));
        set_test.addItem(diBuilder.getDocItem("not-any?"));


        final DocSection maps = new DocSection("Maps", "collections.maps");
        section.addSection(maps);

        final DocSection maps_create = new DocSection("Create", "collections.maps.create");
        maps.addSection(maps_create);
        maps_create.addItem(diBuilder.getDocItem("{}"));
        maps_create.addItem(diBuilder.getDocItem("hash-map"));
        maps_create.addItem(diBuilder.getDocItem("ordered-map"));
        maps_create.addItem(diBuilder.getDocItem("sorted-map"));
        maps_create.addItem(diBuilder.getDocItem("mutable-map"));
        maps_create.addItem(diBuilder.getDocItem("zipmap"));


        final DocSection map_access = new DocSection("Access", "collections.maps.access");
        maps.addSection(map_access);
        map_access.addItem(diBuilder.getDocItem("find"));
        map_access.addItem(diBuilder.getDocItem("get"));
        map_access.addItem(diBuilder.getDocItem("keys"));
        map_access.addItem(diBuilder.getDocItem("vals"));

        final DocSection map_modify = new DocSection("Modify", "collections.maps.modify");
        maps.addSection(map_modify);
        map_modify.addItem(diBuilder.getDocItem("cons"));
        map_modify.addItem(diBuilder.getDocItem("conj"));
        map_modify.addItem(diBuilder.getDocItem("conj!"));
        map_modify.addItem(diBuilder.getDocItem("assoc"));
        map_modify.addItem(diBuilder.getDocItem("assoc!"));
        map_modify.addItem(diBuilder.getDocItem("update"));
        map_modify.addItem(diBuilder.getDocItem("update!"));
        map_modify.addItem(diBuilder.getDocItem("dissoc"));
        map_modify.addItem(diBuilder.getDocItem("dissoc!"));
        map_modify.addItem(diBuilder.getDocItem("into"));
        map_modify.addItem(diBuilder.getDocItem("into!"));
        map_modify.addItem(diBuilder.getDocItem("concat"));
        map_modify.addItem(diBuilder.getDocItem("flatten"));
        map_modify.addItem(diBuilder.getDocItem("filter-k"));
        map_modify.addItem(diBuilder.getDocItem("filter-kv"));
        map_modify.addItem(diBuilder.getDocItem("reduce-kv"));
        map_modify.addItem(diBuilder.getDocItem("merge"));
        map_modify.addItem(diBuilder.getDocItem("merge-with"));
        map_modify.addItem(diBuilder.getDocItem("merge-deep"));
        map_modify.addItem(diBuilder.getDocItem("map-invert"));
        map_modify.addItem(diBuilder.getDocItem("map-keys"));
        map_modify.addItem(diBuilder.getDocItem("map-vals"));
        map_modify.addItem(diBuilder.getDocItem("select-keys"));

        final DocSection map_entries = new DocSection("Entries", "collections.maps.entries");
        maps.addSection(map_entries);
        map_entries.addItem(diBuilder.getDocItem("map-entry"));
        map_entries.addItem(diBuilder.getDocItem("key"));
        map_entries.addItem(diBuilder.getDocItem("val"));
        map_entries.addItem(diBuilder.getDocItem("entries"));
        map_entries.addItem(diBuilder.getDocItem("map-entry?"));

        final DocSection map_nested = new DocSection("Nested", "collections.maps.nested");
        maps.addSection(map_nested);
        map_nested.addItem(diBuilder.getDocItem("get-in"));
        map_nested.addItem(diBuilder.getDocItem("assoc-in"));
        map_nested.addItem(diBuilder.getDocItem("update-in"));
        map_nested.addItem(diBuilder.getDocItem("dissoc-in"));

        final DocSection map_test = new DocSection("Test", "collections.maps.test");
        maps.addSection(map_test);
        map_test.addItem(diBuilder.getDocItem("map?"));
        map_test.addItem(diBuilder.getDocItem("sequential?"));
        map_test.addItem(diBuilder.getDocItem("hash-map?"));
        map_test.addItem(diBuilder.getDocItem("ordered-map?"));
        map_test.addItem(diBuilder.getDocItem("sorted-map?"));
        map_test.addItem(diBuilder.getDocItem("mutable-map?"));
        map_test.addItem(diBuilder.getDocItem("contains?"));
        map_test.addItem(diBuilder.getDocItem("not-contains?"));


        final DocSection stacks = new DocSection("Stack", "collections.stack");
        section.addSection(stacks);

        final DocSection stacks_create = new DocSection("Create", "collections.stack.create");
        stacks.addSection(stacks_create);
        stacks_create.addItem(diBuilder.getDocItem("stack"));

        final DocSection stacks_access = new DocSection("Access", "collections.stack.access");
        stacks.addSection(stacks_access);
        stacks_access.addItem(diBuilder.getDocItem("peek"));
        stacks_access.addItem(diBuilder.getDocItem("pop!"));
        stacks_access.addItem(diBuilder.getDocItem("push!"));
        stacks_access.addItem(diBuilder.getDocItem("into!"));
        stacks_access.addItem(diBuilder.getDocItem("conj!"));
        stacks_access.addItem(diBuilder.getDocItem("count"));

        final DocSection stacks_test = new DocSection("Test", "collections.stack.test");
        stacks.addSection(stacks_test);
        stacks_test.addItem(diBuilder.getDocItem("empty?"));
        stacks_test.addItem(diBuilder.getDocItem("stack?"));


        final DocSection queues = new DocSection("Queue", "collections.queue");
        section.addSection(queues);

        final DocSection queues_create = new DocSection("Create", "collections.queue.create");
        queues.addSection(queues_create);
        queues_create.addItem(diBuilder.getDocItem("queue"));

        final DocSection queues_access = new DocSection("Access", "collections.queue.access");
        queues.addSection(queues_access);
        queues_access.addItem(diBuilder.getDocItem("peek"));
        queues_access.addItem(diBuilder.getDocItem("into!"));
        queues_access.addItem(diBuilder.getDocItem("conj!"));
        queues_access.addItem(diBuilder.getDocItem("count"));

        final DocSection queues_access_sync = new DocSection("Sync", "collections.queue.access.sync");
        queues.addSection(queues_access_sync);
        queues_access_sync.addItem(diBuilder.getDocItem("put!"));
        queues_access_sync.addItem(diBuilder.getDocItem("take!"));

        final DocSection queues_access_async = new DocSection("Async", "collections.queue.access.async");
        queues.addSection(queues_access_async);
        queues_access_async.addItem(diBuilder.getDocItem("offer!"));
        queues_access_async.addItem(diBuilder.getDocItem("poll!"));

        final DocSection queues_process = new DocSection("Process", "collections.queue.process");
        queues.addSection(queues_process);
        queues_process.addItem(diBuilder.getDocItem("docoll"));
        queues_process.addItem(diBuilder.getDocItem("transduce"));
        queues_process.addItem(diBuilder.getDocItem("reduce"));

        final DocSection queues_test = new DocSection("Test", "collections.queue.test");
        queues.addSection(queues_test);
        queues_test.addItem(diBuilder.getDocItem("empty?"));
        queues_test.addItem(diBuilder.getDocItem("queue?"));


        final DocSection dqueues = new DocSection("DelayQueue", "collections.delayqueue");
        section.addSection(dqueues);

        final DocSection dqueues_create = new DocSection("Create", "collections.delayqueue.create");
        dqueues.addSection(dqueues_create);
        dqueues_create.addItem(diBuilder.getDocItem("delay-queue"));

        final DocSection dqueues_access = new DocSection("Access", "collections.delayqueue.access");
        dqueues.addSection(dqueues_access);
        dqueues_access.addItem(diBuilder.getDocItem("peek"));
        dqueues_access.addItem(diBuilder.getDocItem("count"));

        final DocSection dqueues_access_sync = new DocSection("Sync", "collections.delayqueue.access.sync");
        dqueues.addSection(dqueues_access_sync);
        dqueues_access_sync.addItem(diBuilder.getDocItem("put!"));
        dqueues_access_sync.addItem(diBuilder.getDocItem("take!"));

        final DocSection dqueues_access_async = new DocSection("Async", "collections.delayqueue.access.async");
        dqueues.addSection(dqueues_access_async);
        dqueues_access_async.addItem(diBuilder.getDocItem("poll!"));

        final DocSection dqueues_test = new DocSection("Test", "collections.delayqueue.test");
        dqueues.addSection(dqueues_test);
        dqueues_test.addItem(diBuilder.getDocItem("empty?"));
        dqueues_test.addItem(diBuilder.getDocItem("delay-queue?"));


        final DocSection dag = new DocSection("DAG", "directed acyclic graph", "collections.dag");
        section.addSection(dag);

        final DocSection dag_create = new DocSection("Create", "collections.dag.create");
        dag.addSection(dag_create);
        dag_create.addItem(diBuilder.getDocItem("dag/dag"));
        dag_create.addItem(diBuilder.getDocItem("dag/add-edges"));
        dag_create.addItem(diBuilder.getDocItem("dag/add-nodes"));

        final DocSection dag_access = new DocSection("Access", "collections.dag.access");
        dag.addSection(dag_access);
        dag_access.addItem(diBuilder.getDocItem("dag/nodes"));
        dag_access.addItem(diBuilder.getDocItem("dag/edges"));
        dag_access.addItem(diBuilder.getDocItem("dag/roots"));
        dag_access.addItem(diBuilder.getDocItem("count"));

        final DocSection dag_children = new DocSection("Children", "collections.dag.children");
        dag.addSection(dag_children);
        dag_children.addItem(diBuilder.getDocItem("dag/children"));
        dag_children.addItem(diBuilder.getDocItem("dag/direct-children"));

        final DocSection dag_parents = new DocSection("Parents", "collections.dag.parents");
        dag.addSection(dag_parents);
        dag_parents.addItem(diBuilder.getDocItem("dag/parents"));
        dag_parents.addItem(diBuilder.getDocItem("dag/direct-parents"));

        final DocSection dag_sort = new DocSection("Sort", "collections.dag.sort");
        dag.addSection(dag_sort);
        dag_sort.addItem(diBuilder.getDocItem("dag/topological-sort"));
        dag_sort.addItem(diBuilder.getDocItem("dag/compare-fn"));

        final DocSection dag_test = new DocSection("Test", "collections.dag.test");
        dag.addSection(dag_test);
        dag_test.addItem(diBuilder.getDocItem("dag/dag?"));
        dag_test.addItem(diBuilder.getDocItem("dag/node?"));
        dag_test.addItem(diBuilder.getDocItem("dag/edge?"));
        dag_test.addItem(diBuilder.getDocItem("dag/parent-of?"));
        dag_test.addItem(diBuilder.getDocItem("dag/child-of?"));
        dag_test.addItem(diBuilder.getDocItem("empty?"));

        return section;
    }

    private final DocItemBuilder diBuilder;
}
