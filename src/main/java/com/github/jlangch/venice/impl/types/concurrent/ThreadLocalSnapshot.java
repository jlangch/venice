package com.github.jlangch.venice.impl.types.concurrent;

import java.util.Map;

import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.util.MeterRegistry;
import com.github.jlangch.venice.javainterop.IInterceptor;


public class ThreadLocalSnapshot {

	public ThreadLocalSnapshot(
			final Map<VncKeyword,VncVal> values,
			final DebugAgent agent,
			final IInterceptor interceptor,
			final MeterRegistry meterRegistry
	) {
		this.values = values;
		this.agent = agent;
		this.interceptor = interceptor;
		this.meterRegistry = meterRegistry;
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



	private final Map<VncKeyword,VncVal> values;
	private final DebugAgent agent;
	private final IInterceptor interceptor;
	private final MeterRegistry meterRegistry;
}
