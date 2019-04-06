/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl.functions;

import static com.github.jlangch.venice.impl.functions.FunctionsUtil.assertArity;
import static com.github.jlangch.venice.impl.types.Constants.Nil;

import java.util.List;
import java.util.Map;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.javainterop.JavaInteropUtil;
import com.github.jlangch.venice.impl.types.VncDouble;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncInteger;
import com.github.jlangch.venice.impl.types.VncJavaObject;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncHashMap;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.util.Coerce;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.reflect.ReflectionTypes;


public class ArrayFunctions {
	
	public static VncFunction aset = 
		new VncFunction(
				"aset", 
				VncFunction
					.meta()
					.arglists("(aset array idx val)")		
					.doc(
						"Sets the value at the index of an array")
					.examples(
						"(aset (long-array '(1 2 3 4 5)) 1 20)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();

				final Object delegate = jo.getDelegate();
				final Class<?> delegateClass = delegate.getClass();
				
				if (!ReflectionTypes.isArrayType(delegateClass)) {
					throw new VncException(String.format(
							"The array argument (%s) is not an array",
							Types.getType(jo)));
				}

				final Class<?> componentType = delegateClass.getComponentType();

				if (componentType == String.class) {
					((String[])delegate)[idx.getValue()] = Coerce.toVncString(val).getValue();
				}
				else if (componentType == int.class) {
					((int[])delegate)[idx.getValue()] = Numeric.toInteger(val).getValue();
				}
				else if (componentType == long.class) {
					((long[])delegate)[idx.getValue()] = Numeric.toLong(val).getValue();
				}
				else if (componentType == float.class) {
					((float[])delegate)[idx.getValue()] = Numeric.toDouble(val).getValue().floatValue();
				}
				else if (componentType == double.class) {
					((double[])delegate)[idx.getValue()] = Numeric.toDouble(val).getValue();
				}
				else {
					((Object[])delegate)[idx.getValue()] = val.convertToJavaObject();
				}
				
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction aget = 
			new VncFunction(
					"aget", 
					VncFunction
						.meta()
						.arglists("(aget array idx)")		
						.doc(
							"Returns the value at the index of an array of Java Objects")
						.examples(
							"(aget (long-array '(1 2 3 4 5)) 1)")
						.build()
			) {		
				public VncVal apply(final VncList args) {			
					assertArity("aget", args, 2);

					final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
					final VncInteger idx = Numeric.toInteger(args.second());

					final Object delegate = jo.getDelegate();
					final Class<?> delegateClass = delegate.getClass();
					
					if (!ReflectionTypes.isArrayType(delegateClass)) {
						throw new VncException(String.format(
								"The array argument (%s) is not an array",
								Types.getType(jo)));
					}

					final Class<?> componentType = delegateClass.getComponentType();

					if (componentType == String.class) {
						return new VncString(((String[])delegate)[idx.getValue()]);
					}
					else if (componentType == int.class) {
						return new VncInteger(((int[])delegate)[idx.getValue()]);
					}
					else if (componentType == long.class) {
						return new VncLong(((long[])delegate)[idx.getValue()]);
					}
					else if (componentType == float.class) {
						return new VncDouble(((float[])delegate)[idx.getValue()]);
					}
					else if (componentType == double.class) {
						return new VncDouble(((double[])delegate)[idx.getValue()]);
					}
					else {
						return JavaInteropUtil.convertToVncVal((((Object[])delegate)[idx.getValue()]));
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};
			
	public static VncFunction alength = 
			new VncFunction(
					"aget", 
					VncFunction
						.meta()
						.arglists("(alength array)")		
						.doc(
							"Returns the length of an array")
						.examples(
							"(alength (long-array '(1 2 3 4 5)))")
						.build()
			) {		
				public VncVal apply(final VncList args) {			
					assertArity("alength", args, 1);

					final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
	
					final Object delegate = jo.getDelegate();
					final Class<?> delegateClass = delegate.getClass();
					
					if (!ReflectionTypes.isArrayType(delegateClass)) {
						throw new VncException(String.format(
								"The array argument (%s) is not an array",
								Types.getType(jo)));
					}

					final Class<?> componentType = delegateClass.getComponentType();

					if (componentType == String.class) {
						return new VncLong(((String[])delegate).length);
					}
					else if (componentType == int.class) {
						return new VncLong(((int[])delegate).length);
					}
					else if (componentType == long.class) {
						return new VncLong(((long[])delegate).length);
					}
					else if (componentType == float.class) {
						return new VncLong(((float[])delegate).length);
					}
					else if (componentType == double.class) {
						return new VncLong(((double[])delegate).length);
					}
					else {
						return new VncLong(((Object[])delegate).length);
					}
				}
		
			    private static final long serialVersionUID = -1848883965231344442L;
			};
			
	public static VncFunction object_array = 
		new VncFunction(
				"object-array", 
				VncFunction
					.meta()
					.arglists("(object-array coll)", "(object-array len)")		
					.doc(
						"Returns an array of Java Objects containing the contents of coll "
								+ "or returns an array with the given length")
					.examples(
						"(object-array '(1 2 3 4 5))",
						"(object-array '(1 2.0 3.45M \"4\" true))",
						"(object-array 10)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("object-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new Object[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final Object[] arr = new Object[list.size()];
				
					int ii=0;
					for(VncVal v : list) {
						arr[ii++] = v.convertToJavaObject();
					}

					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction string_array = 
		new VncFunction(
				"string-array", 
				VncFunction
					.meta()
					.arglists("(string-array coll)", "(string-array len)")		
					.doc(
						"Returns an array of Java strings containing the contents of coll"
							+ "or returns an array with the given length")
					.examples(
						"(string-array '(\"1\" \"2\" \"3\"))",
						"(string-array 10)") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("string-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new String[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final String[] arr = new String[list.size()];
					
					int ii=0;
					for(VncVal v : list) {
						if (!Types.isVncString(v)) {
							throw new VncException(String.format(
									"The value at pos %d in the collection is not a string",
									ii));
						}
						arr[ii++] = ((VncString)v).getValue();
					}
					
					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction int_array = 
		new VncFunction(
				"int-array", 
				VncFunction
					.meta()
					.arglists("(int-array coll)", "(int-array len)")		
					.doc(
						"Returns an array of Java ints containing the contents of coll"
							+ "or returns an array with the given length")
					.examples(
						"(int-array '(1I 2I 3I))",
						"(int-array '(1I 2 3.2 3.56M))",
						"(int-array 10)") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("int-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new int[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final int[] arr = new int[list.size()];
					
					int ii=0;
					for(VncVal v : list) {
						if (v == Nil || !Types.isVncNumber(v)) {
							throw new VncException(String.format(
									"The value at pos %d in the collection is not a number",
									ii));
						}
						arr[ii++] = Numeric.toInteger(v).getValue().intValue();
					}
					
					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
	public static VncFunction long_array = 
		new VncFunction(
				"long-array", 
				VncFunction
					.meta()
					.arglists("(long-array coll)", "(long-array len)")		
					.doc(
						"Returns an array of Java longs containing the contents of coll"
							+ "or returns an array with the given length")
					.examples(
						"(long-array '(1 2 3))",
						"(long-array '(1I 2 3.2 3.56M))",
						"(long-array 10)") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("long-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new long[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final long[] arr = new long[list.size()];
					
					int ii=0;
					for(VncVal v : list) {
						if (v == Nil || !Types.isVncNumber(v)) {
							throw new VncException(String.format(
									"The value at pos %d in the collection is not a number",
									ii));
						}
						arr[ii++] = Numeric.toLong(v).getValue().longValue();
					}
					
					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction float_array = 
		new VncFunction(
				"float-array", 
				VncFunction
					.meta()
					.arglists("(float-array coll)", "(float-array len)")		
					.doc(
						"Returns an array of Java floats containing the contents of coll"
							+ "or returns an array with the given length")
					.examples(
						"(float-array '(1.0 2.0 3.0))",
						"(float-array '(1I 2 3.2 3.56M))",
						"(float-array 10)") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("float-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new float[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final float[] arr = new float[list.size()];
					
					int ii=0;
					for(VncVal v : list) {
						if (v == Nil || !Types.isVncNumber(v)) {
							throw new VncException(String.format(
									"The value at pos %d in the collection is not a number",
									ii));
						}
						arr[ii++] = Numeric.toDouble(v).getValue().floatValue();
					}
					
					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
	public static VncFunction double_array = 
		new VncFunction(
				"double-array", 
				VncFunction
					.meta()
					.arglists("(double-array coll)", "(double-array len)")		
					.doc(
						"Returns an array of Java doubles containing the contents of coll"
							+ "or returns an array with the given length")
					.examples(
						"(double-array '(1.0 2.0 3.0))",
						"(double-array '(1I 2 3.2 3.56M))",
						"(double-array 10)") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("double-array", args, 1);

				final VncVal arg = args.first();
				
				if (Types.isVncLong(arg)) {
					return new VncJavaObject(new double[((VncLong)arg).getIntValue()]);
				}
				else {
					final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
					
					final double[] arr = new double[list.size()];
					
					int ii=0;
					for(VncVal v : list) {
						if (v == Nil || !Types.isVncNumber(v)) {
							throw new VncException(String.format(
									"The value at pos %d in the collection is not a number",
									ii));
						}
						arr[ii++] = Numeric.toDouble(v).getValue().doubleValue();
					}
					
					return new VncJavaObject(arr);
				}
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
			
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("aget",			aget)
					.put("aset",			aset)
					.put("alength",			alength)
					.put("object-array",	object_array)
					.put("string-array",	string_array)
					.put("int-array",		int_array)
					.put("long-array",		long_array)
					.put("float-array",		float_array)
					.put("double-array",	double_array)
					.toMap();	
}
