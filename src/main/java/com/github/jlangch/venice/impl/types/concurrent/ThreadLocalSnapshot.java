package com.github.jlangch.venice.impl.types.concurrent;

import java.util.Map;

import com.github.jlangch.venice.impl.debug.agent.DebugAgent;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncVal;


public class ThreadLocalSnapshot {

	public ThreadLocalSnapshot(
			final Map<VncKeyword,VncVal> values,
			final DebugAgent agent
	) {
		this.values = values;
		this.agent = agent;
	}

	
	public Map<VncKeyword, VncVal> getValues() {
		return values;
	}
	
	public DebugAgent getAgent() {
		return agent;
	}



	private final Map<VncKeyword,VncVal> values;
	private final DebugAgent agent;
}
