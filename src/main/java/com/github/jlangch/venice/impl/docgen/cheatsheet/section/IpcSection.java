/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2026 Venice
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


public class IpcSection implements ISectionBuilder {

    public IpcSection(final DocItemBuilder diBuilder) {
        this.diBuilder = diBuilder;
    }

    @Override
    public DocSection section() {
        final DocSection section = new DocSection("IPC", "Inter-Process-Communication", "ipc");

        final DocSection all = new DocSection("", id());
        section.addSection(all);

        final DocSection conn = new DocSection("Connection", "ipc.conn");
        all.addSection(conn);
        conn.addItem(diBuilder.getDocItem("ipc/server", false));
        conn.addItem(diBuilder.getDocItem("ipc/client", false));
        conn.addItem(diBuilder.getDocItem("ipc/clone", false));
        conn.addItem(diBuilder.getDocItem("ipc/close", false));
        conn.addItem(diBuilder.getDocItem("ipc/running?", false));

        final DocSection send = new DocSection("Send", "ipc.send");
        all.addSection(send);
        send.addItem(diBuilder.getDocItem("ipc/send", false));
        send.addItem(diBuilder.getDocItem("ipc/send-async", false));
        send.addItem(diBuilder.getDocItem("ipc/send-oneway", false));

        final DocSection pub = new DocSection("Pub / Sub", "ipc.publish");
        all.addSection(pub);
        pub.addItem(diBuilder.getDocItem("ipc/publish", false));
        pub.addItem(diBuilder.getDocItem("ipc/publish-async", false));
        pub.addItem(diBuilder.getDocItem("ipc/subscribe", false));
        pub.addItem(diBuilder.getDocItem("ipc/unsubscribe", false));

        final DocSection offer = new DocSection("Offer / Poll", "ipc.offer");
        all.addSection(offer);
        offer.addItem(diBuilder.getDocItem("ipc/offer", false));
        offer.addItem(diBuilder.getDocItem("ipc/offer-async", false));
        offer.addItem(diBuilder.getDocItem("ipc/poll", false));
        offer.addItem(diBuilder.getDocItem("ipc/poll-async", false));

        final DocSection auth = new DocSection("Authenticator", "ipc.auth");
        all.addSection(auth);
        auth.addItem(diBuilder.getDocItem("ipc/authenticator", false));
        auth.addItem(diBuilder.getDocItem("ipc/load-authenticator", false));
        auth.addItem(diBuilder.getDocItem("ipc/store-authenticator", false));
        auth.addItem(diBuilder.getDocItem("ipc/add-credentials", false));
        auth.addItem(diBuilder.getDocItem("ipc/remove-credentials", false));
        auth.addItem(diBuilder.getDocItem("ipc/clear-credentials", false));

        final DocSection acl = new DocSection("ACL", "ipc.acl");
        all.addSection(acl);
        acl.addItem(diBuilder.getDocItem("ipc/add-acl", false));
        acl.addItem(diBuilder.getDocItem("ipc/remove-acl", false));
        acl.addItem(diBuilder.getDocItem("ipc/default-acl", false));

        final DocSection msg = new DocSection("Messages", "ipc.message");
        all.addSection(msg);
        msg.addItem(diBuilder.getDocItem("ipc/text-message", false));
        msg.addItem(diBuilder.getDocItem("ipc/plain-text-message", false));
        msg.addItem(diBuilder.getDocItem("ipc/binary-message", false));
        msg.addItem(diBuilder.getDocItem("ipc/venice-message", false));
        msg.addItem(diBuilder.getDocItem("ipc/message-expired?", false));
        msg.addItem(diBuilder.getDocItem("ipc/message-field", false));
        msg.addItem(diBuilder.getDocItem("ipc/message->map", false));
        msg.addItem(diBuilder.getDocItem("ipc/message->json", false));
        msg.addItem(diBuilder.getDocItem("ipc/oneway?", false));
        msg.addItem(diBuilder.getDocItem("ipc/response-ok?", false));
        msg.addItem(diBuilder.getDocItem("ipc/response-err?", false));
        msg.addItem(diBuilder.getDocItem("ipc/message-size", false));

        final DocSection queue = new DocSection("Queues", "ipc.queue");
        all.addSection(queue);
        queue.addItem(diBuilder.getDocItem("ipc/create-queue", false));
        queue.addItem(diBuilder.getDocItem("ipc/create-temporary-queue", false));
        queue.addItem(diBuilder.getDocItem("ipc/remove-queue", false));
        queue.addItem(diBuilder.getDocItem("ipc/exists-queue?", false));
        queue.addItem(diBuilder.getDocItem("ipc/queue-status", false));

        final DocSection topic = new DocSection("Topics", "ipc.topic");
        all.addSection(topic);
        topic.addItem(diBuilder.getDocItem("ipc/create-topic", false));
        topic.addItem(diBuilder.getDocItem("ipc/remove-topic", false));
        topic.addItem(diBuilder.getDocItem("ipc/exists-topic?", false));

        final DocSection func = new DocSection("Functions", "ipc.function");
        all.addSection(func);
        func.addItem(diBuilder.getDocItem("ipc/create-function", false));
        func.addItem(diBuilder.getDocItem("ipc/remove-function", false));
        func.addItem(diBuilder.getDocItem("ipc/exists-function?", false));

        final DocSection bench = new DocSection("Benchmark", "ipc.bench");
        all.addSection(bench);
        bench.addItem(diBuilder.getDocItem("ipc/benchmark", false));

        final DocSection util = new DocSection("Util", "ipc.util");
        all.addSection(util);
        util.addItem(diBuilder.getDocItem("ipc/server-status", false));
        util.addItem(diBuilder.getDocItem("ipc/server-thread-pool-statistics", false));

        return section;
    }

    private String id() {
        return diBuilder.id();
    }

    private final DocItemBuilder diBuilder;
}
