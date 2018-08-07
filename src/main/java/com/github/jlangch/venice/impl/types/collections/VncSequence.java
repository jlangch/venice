package com.github.jlangch.venice.impl.types.collections;

import java.util.List;

import com.github.jlangch.venice.impl.types.VncVal;


public abstract class VncSequence extends VncCollection {

	public abstract List<VncVal> getList();

}
