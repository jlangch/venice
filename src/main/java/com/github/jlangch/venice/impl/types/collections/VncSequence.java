package com.github.jlangch.venice.impl.types.collections;

import java.util.List;
import java.util.stream.Stream;

import com.github.jlangch.venice.impl.types.VncVal;


public abstract class VncSequence extends VncCollection {

	public abstract List<VncVal> getList();

	public  Stream<VncVal> stream() {
		return getList().stream();
	}
}
