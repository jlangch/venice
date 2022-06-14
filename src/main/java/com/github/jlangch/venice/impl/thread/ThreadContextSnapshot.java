package com.github.jlangch.venice.impl.thread;

import java.util.Map;

import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.namespaces.Namespace;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.javainterop.IInterceptor;


/**
 * Holds a <code>ThreadContext</code> snapshot from a parent to be inherited
 * to a child thread, when functions are to be run in other threads.
 */
public class ThreadContextSnapshot {

    public ThreadContextSnapshot(
            final Long threadID,
            final Namespace ns,
            final Map<VncKeyword,VncVal> values,
            final DebugAgent agent,
            final IInterceptor interceptor,
            final MeterRegistry meterRegistry
    ) {
        this.threadID = threadID;
        this.ns = ns;
        this.values = values;
        this.agent = agent;
        this.interceptor = interceptor;
        this.meterRegistry = meterRegistry;
    }


    public long getThreadID() {
        return threadID;
    }

    public Namespace getNamespace() {
        return ns;
    }

    public Map<VncKeyword, VncVal> getValues() {
        return values;
    }

    public DebugAgent getAgent() {
        return agent;
    }

    public IInterceptor getInterceptor() {
        return interceptor;
    }

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    public boolean isDifferentFromCurrentThread() {
        return threadID != Thread.currentThread().getId();
    }

    public boolean isSameAsCurrentThread() {
        return threadID == Thread.currentThread().getId();
    }



    private final Long threadID;
    private final Namespace ns;
    private final Map<VncKeyword,VncVal> values;
    private final DebugAgent agent;
    private final IInterceptor interceptor;
    private final MeterRegistry meterRegistry;
}
