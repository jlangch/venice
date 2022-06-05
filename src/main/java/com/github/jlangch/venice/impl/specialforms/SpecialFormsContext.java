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
package com.github.jlangch.venice.impl.specialforms;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.jlangch.venice.impl.FunctionBuilder;
import com.github.jlangch.venice.impl.IFormEvaluator;
import com.github.jlangch.venice.impl.ISequenceValuesEvaluator;
import com.github.jlangch.venice.impl.IValuesEvaluator;
import com.github.jlangch.venice.impl.IVeniceInterpreter;
import com.github.jlangch.venice.impl.NamespaceRegistry;
import com.github.jlangch.venice.impl.types.custom.CustomWrappableTypes;
import com.github.jlangch.venice.impl.util.MeterRegistry;


public class SpecialFormsContext {

	public SpecialFormsContext(
			final IVeniceInterpreter interpreter,
			final IFormEvaluator evaluator,
			final IValuesEvaluator valuesEvaluator,
			final ISequenceValuesEvaluator sequenceValuesEvaluator,
			final FunctionBuilder functionBuilder,
			final NamespaceRegistry nsRegistry,
			final MeterRegistry meterRegistry,
			final AtomicBoolean sealedSystemNS
	) {
		this.interpreter = interpreter;
		this.evaluator = evaluator;
		this.valuesEvaluator = valuesEvaluator;
		this.functionBuilder = functionBuilder;
		this.nsRegistry = nsRegistry;
		this.meterRegistry = meterRegistry;
		this.sealedSystemNS = sealedSystemNS;
	}
	

	public CustomWrappableTypes getWrappableTypes() {
		return wrappableTypes;
	}
	
	public NamespaceRegistry getNsRegistry() {
		return nsRegistry;
	}
	
	public MeterRegistry getMeterRegistry() {
		return meterRegistry;
	}
	
	public AtomicBoolean getSealedSystemNS() {
		return sealedSystemNS;
	}
	
	public IVeniceInterpreter getInterpreter() {
		return interpreter;
	}
	
	public IFormEvaluator getEvaluator() {
		return evaluator;
	}
	
	public IValuesEvaluator getValuesEvaluator() {
		return valuesEvaluator;
	}

	public FunctionBuilder getFunctionBuilder() {
		return functionBuilder;
	}


	private final CustomWrappableTypes wrappableTypes = new CustomWrappableTypes();
	private final NamespaceRegistry nsRegistry;
	private final MeterRegistry meterRegistry;
	private final AtomicBoolean sealedSystemNS;

	private final IVeniceInterpreter interpreter;
	private final IFormEvaluator evaluator;
	private final IValuesEvaluator valuesEvaluator;
	private final FunctionBuilder functionBuilder;
}
