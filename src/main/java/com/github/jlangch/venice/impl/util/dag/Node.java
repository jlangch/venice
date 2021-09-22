/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2021 Venice
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
package com.github.jlangch.venice.impl.util.dag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public class Node<T> {

	public Node(final T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public List<Node<T>> getParents() {
		return Collections.unmodifiableList(parents);
	}

	public List<Node<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void addChild(final Node<T> child) {
		if (child == this) {
			throw new DagCycleException(this.toString() + "->" + this.toString());
		}
		
		children.add(child);
		
		if (child.getParents().contains(this)) {
			return;
		}
		else {
			child.addParent(this);
		}
	}

	public void visitDepthFirst(
			final Consumer<Node<T>> consumer, 
			final Set<Node<T>> visited
	) {
		if (visited.contains(this)) return;
		consumer.accept(this);
		visited.add(this);
		for (Node<T> node : children) {
			if (visited.contains(node)) continue;
			node.visitDepthFirst(consumer, visited);
		}
	}

	@Override
	public String toString() {
		return "Node{" + "value=" + value.toString() + '}';
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node<T> other = (Node<T>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}


	private void addParent(final Node<T> parent) {
		if (parent == this) {
			throw new DagCycleException(this.toString() + "->" + this.toString());
		}
		
		parents.add(parent);
		if (parent.getChildren().contains(this)) {
			return;
		}
		else {
			parent.addChild(this);
		}
	}


	private final List<Node<T>> parents = new LinkedList<>();
	private final List<Node<T>> children = new LinkedList<>();
	private final T value;
}