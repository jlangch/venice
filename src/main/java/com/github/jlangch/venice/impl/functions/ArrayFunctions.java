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
						"Sets the value at the index of an array of Java Objects")
					.examples(
						"(aset (to-array '(1 2 3 4 5)) 1 20)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();
				
				final Object[] array = (Object[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = val.convertToJavaObject();
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction aset_string = 
		new VncFunction(
				"aset-string", 
				VncFunction
					.meta()
					.arglists("(aset-string array idx val)")		
					.doc(
						"Sets the value at the index of an array of Java strings")
					.examples(
						"(aset-string (string-array '(\"1\" \"2\" \"3\") 1 \"20\")")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset-string", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncString val = Coerce.toVncString(args.third());
				
				final String[] array = (String[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = val.getValue();
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction aset_int = 
		new VncFunction(
				"aset-int", 
				VncFunction
					.meta()
					.arglists("(aset-int array idx val)")		
					.doc(
						"Sets the value at the index of an array of Java ints")
					.examples(
						"(aset-int (int-array '(1I 2I 3I 4I 5I)) 1 20I)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset-int", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();
				
				final int[] array = (int[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = Numeric.toInteger(val).getValue();
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction aset_long = 
		new VncFunction(
				"aset-long", 
				VncFunction
					.meta()
					.arglists("(aset-long array idx val)")		
					.doc(
						"Sets the value at the index of an array of Java longs")
					.examples(
						"(aset-long (long-array '(1 2 3 4 5)) 1 20)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset-long", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();
				
				final long[] array = (long[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = Numeric.toLong(val).getValue();
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction aset_float = 
		new VncFunction(
				"aset-float", 
				VncFunction
					.meta()
					.arglists("(aset-float array idx val)")		
					.doc(
						"Sets the value at the index of an array of Java floats")
					.examples(
						"(aset-float (float-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.0)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset-float", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();
				
				final float[] array = (float[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = Numeric.toDouble(val).getValue().floatValue();
				return jo;
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction aset_double = 
		new VncFunction(
				"aset-double", 
				VncFunction
					.meta()
					.arglists("(aset-double array idx val)")		
					.doc(
						"Sets the value at the index of an array of Java doubles")
					.examples(
						"(aset-double (double-array '(1.0 2.0 3.0 4.0 5.0)) 1 20.0)")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("aset-double", args, 3);

				final VncJavaObject jo = Coerce.toVncJavaObject(args.first());
				final VncInteger idx = Numeric.toInteger(args.second());
				final VncVal val = args.third();
				
				final double[] array = (double[])jo.getDelegate();
				
				array[idx.getValue().intValue()] = Numeric.toDouble(val).getValue();
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
							"(aget (to-array '(1 2 3 4 5)) 1)")
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
			
	public static VncFunction to_array = 
		new VncFunction(
				"to-array", 
				VncFunction
					.meta()
					.arglists("(to-array coll)")		
					.doc(
						"Returns an array of Java Objects containing the contents of coll")
					.examples(
						"(to-array '(1 2 3 4 5))",
						"(to-array '(1 2.0 3.45M \"4\" true))")
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("to-array", args, 1);

				final List<VncVal> list = Coerce.toVncSequence(args.first()).getList();
				
				final Object[] arr = new Object[list.size()];
			
				int ii=0;
				for(VncVal v : list) {
					arr[ii++] = v.convertToJavaObject();
				}

				return new VncJavaObject(arr);
			}
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
	
	public static VncFunction string_array = 
		new VncFunction(
				"string-array", 
				VncFunction
					.meta()
					.arglists("(string-array coll)")		
					.doc(
						"Returns an array of Java strings containing the contents of coll")
					.examples(
						"(string-array '(\"1\" \"2\" \"3\"))") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("string-array", args, 1);

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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction int_array = 
		new VncFunction(
				"int-array", 
				VncFunction
					.meta()
					.arglists("(int-array coll)")		
					.doc(
						"Returns an array of Java ints containing the contents of coll")
					.examples(
						"(int-array '(1I 2I 3I))",
						"(int-array '(1I 2 3.2 3.56M))") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("int-array", args, 1);

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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
	public static VncFunction long_array = 
		new VncFunction(
				"long-array", 
				VncFunction
					.meta()
					.arglists("(long-array coll)")		
					.doc(
						"Returns an array of Java longs containing the contents of coll")
					.examples(
						"(long-array '(1 2 3))",
						"(long-array '(1I 2 3.2 3.56M))") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("long-array", args, 1);

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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
		
	public static VncFunction float_array = 
		new VncFunction(
				"float-array", 
				VncFunction
					.meta()
					.arglists("(float-array coll)")		
					.doc(
						"Returns an array of Java floats containing the contents of coll")
					.examples(
						"(float-array '(1.0 2.0 3.0))",
						"(float-array '(1I 2 3.2 3.56M))") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("float-array", args, 1);
	
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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
	public static VncFunction double_array = 
		new VncFunction(
				"double-array", 
				VncFunction
					.meta()
					.arglists("(double-array coll)")		
					.doc(
						"Returns an array of Java doubles containing the contents of coll")
					.examples(
						"(double-array '(1.0 2.0 3.0))",
						"(double-array '(1I 2 3.2 3.56M))") 
					.build()
		) {		
			public VncVal apply(final VncList args) {			
				assertArity("double-array", args, 1);
	
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
	
		    private static final long serialVersionUID = -1848883965231344442L;
		};
			
			
	///////////////////////////////////////////////////////////////////////////
	// types_ns is namespace of type functions
	///////////////////////////////////////////////////////////////////////////

	public static Map<VncVal, VncVal> ns = 
			new VncHashMap.Builder()
					.put("aget",			aget)
					.put("aset",			aset)
					.put("aset-string",		aset_string)
					.put("aset-int",		aset_int)
					.put("aset-long",		aset_long)
					.put("aset-float",		aset_float)
					.put("aset-double",		aset_double)
					.put("to-array",		to_array)
					.put("string-array",	string_array)
					.put("int-array",		int_array)
					.put("long-array",		long_array)
					.put("float-array",		float_array)
					.put("double-array",	double_array)
					.toMap();	
}
