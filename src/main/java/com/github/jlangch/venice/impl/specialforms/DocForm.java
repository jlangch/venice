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
package com.github.jlangch.venice.impl.specialforms;

import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.Modules;
import com.github.jlangch.venice.impl.ansi.AnsiColorTheme;
import com.github.jlangch.venice.impl.ansi.AnsiColorThemes;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.reader.HighlightItem;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.types.Constants;
import com.github.jlangch.venice.impl.types.VncFunction;
import com.github.jlangch.venice.impl.types.VncKeyword;
import com.github.jlangch.venice.impl.types.VncLong;
import com.github.jlangch.venice.impl.types.VncString;
import com.github.jlangch.venice.impl.types.VncSymbol;
import com.github.jlangch.venice.impl.types.VncVal;
import com.github.jlangch.venice.impl.types.collections.VncList;
import com.github.jlangch.venice.impl.types.collections.VncSet;
import com.github.jlangch.venice.impl.types.custom.VncChoiceTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncCustomTypeDef;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.StringUtil;


public class DocForm {

	public static VncString doc(final VncVal ref, final Env env) {			
		if (Types.isVncSymbol(ref)) {
			return docForSymbol((VncSymbol)ref, env);
		}
		else if (Types.isVncKeyword(ref)) {
			return docForKeyword((VncKeyword)ref, env);
		}
		else if (Types.isVncString(ref)) {
			return docForSymbol(((VncString)ref).toSymbol(), env);
		}
		else {
			// last resort
			final VncString name = (VncString)CoreFunctions.name.apply(VncList.of(ref));
			return docForSymbol(name.toSymbol(), env);
		}
	}

	public static VncString highlight(
			final VncString form, 
			final Env env
	) {
		final AnsiColorTheme theme = AnsiColorThemes.getTheme(getColorTheme(env));

		if (theme == null) {
			return form;
		}
		else {			
			final List<HighlightItem> items = HighlightParser.parse(form.getValue());
			
			return new VncString(
					AnsiColorTheme.ANSI_RESET +
					items.stream()
						 .map(it -> theme.style(it.getForm(), it.getClazz()))
						 .collect(Collectors.joining()));
		}
	}

	private static VncString docForSymbol(final VncSymbol sym, final Env env) {
		VncVal docVal = SpecialFormsDoc.ns.get(sym); // special form?
		if (docVal != null) {
			return docForSymbolVal(docVal, env);
		}
		else {
			try {
				docVal = env.get(sym);
				return docForSymbolVal(docVal, env);
			}
			catch(VncException ex) {
				final String simpleName = sym.getSimpleName();
				
				final List<String> candidates = 
					env.getAllGlobalFunctionSymbols()
					   .stream()
					   .filter(s -> s.getSimpleName().equals(simpleName))
					   .limit(5)
					   .map(s -> "   " + s.getQualifiedName())
					   .collect(Collectors.toList());
				
				if (candidates.isEmpty()) {
					throw ex;
				}
				else {
					return new VncString(
							String.format("Symbol '%s' not found.\n\n", sym.getQualifiedName())
							+ "Did you mean?\n"
							+ String.join("\n", candidates)
							+ "\n");
				}
			}
		}
	}

	private static VncString docForSymbolVal(final VncVal symVal, final Env env) {
		final boolean repl = isREPL(env);		

		// if we run in a REPL use the effective terminal width for rendering
		final int width = repl ? replTerminalWidth(env) : 80;
		
		// TODO: Markdown renderer
		return getDoc(symVal, width);
	}
	
	private static VncString docForKeyword(final VncKeyword keyword, final Env env) {
		if (Modules.isValidModule(keyword)) {
			return docForModule(keyword, env);
		}
		else {
			return docForCustomType(keyword, env);
		}
	}

	private static VncString docForModule(
			final VncKeyword module, 
			final Env env
	) {
		final String form = ModuleLoader.loadModule(module.getValue());
		
		final AnsiColorTheme theme = AnsiColorThemes.getTheme(getColorTheme(env));

		if (theme == null) {
			return new VncString(form);
		}
		else {			
			final List<HighlightItem> items = HighlightParser.parse("(do " + form + ")");
			
			return new VncString(
					AnsiColorTheme.ANSI_RESET +
					items.subList(3, items.size()-1)
						 .stream()
						 .map(it -> theme.style(it.getForm(), it.getClazz()))
						 .collect(Collectors.joining()));
		}
	}
	
	private static VncString docForCustomType(
			final VncKeyword type, 
			final Env env
	) {
		final VncVal tdef = env.getGlobalOrNull(type.toSymbol());

		if (tdef == null) {
			if (type.hasNamespace()) {
				throw new VncException(String.format(
						":%s is not a custom type. No documentation available!",
						type.getValue()));
			}
			else {
				throw new VncException(String.format(
						":%s is not a custom type. Please qualify the type with its namespace!",
						type.getValue()));
			}
		}
		else if (tdef instanceof VncCustomTypeDef) {
			final VncCustomTypeDef typeDef = (VncCustomTypeDef)tdef;
			final StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Custom type :%s\n", type.getValue()));
			sb.append("Fields: \n");
			typeDef.getFieldDefs().forEach(f -> sb.append(String.format(
																"   %s :%s\n", 
																f.getName().getValue(),
																f.getType().getValue())));
			if (typeDef.getValidationFn() != null) {
				sb.append(String.format("Validation function: :%s\n", typeDef.getValidationFn().getQualifiedName()));
			}
			
			return new VncString(sb.toString());
		}
		else if (tdef instanceof VncWrappingTypeDef) {
			final VncWrappingTypeDef typeDef = (VncWrappingTypeDef)tdef;
			final StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Custom wrapped type :%s\n", type.getValue()));
			sb.append(String.format("Base type :%s\n", typeDef.getBaseType().getValue()));
			if (typeDef.getValidationFn() != null) {
				sb.append(String.format("Validation function: :%s\n", typeDef.getValidationFn().getQualifiedName()));
			}
			
			return new VncString(sb.toString());
		}
		else if (tdef instanceof VncChoiceTypeDef) {
			final VncChoiceTypeDef typeDef = (VncChoiceTypeDef)tdef;
			
			final VncSet types = typeDef.typesOnly();
			final VncSet values = typeDef.valuesOnly();
			
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("Custom choice type :%s\n", type.getValue()));
			if (!types.isEmpty()) {
				sb.append("Types: \n");
				typeDef.typesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
			}
			if (!values.isEmpty()) {
				sb.append("Values: \n");
				typeDef.valuesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
			}
			
			return new VncString(sb.toString());
		}
		else {
			throw new VncException(String.format(
					":%s is not a custom type. Please qualify the type with its namespace!",
					type.getValue()));
		}
	}

	private static String getColorTheme(final Env env) {
		// Note: there is a color theme only if we're running in a REPL!
		
		if (isREPL(env)) {
			final VncVal theme = env.get(new VncSymbol("*repl-color-theme*"));
			if (Types.isVncKeyword(theme)) {
				final String sTheme = ((VncKeyword)theme).getValue();
				return "none".equals(sTheme) ? null : sTheme;
			}
		}
				
		return null;
	}
	
	private static boolean isREPL(final Env env) {
		final VncVal runMode = env.get(new VncSymbol("*run-mode*"));
		if (Types.isVncKeyword(runMode)) {
			final String sRunMode = ((VncKeyword)runMode).getValue();
			return ("repl".equals(sRunMode));
		}
		
		return false;
	}
	
	private static int replTerminalWidth(final Env env) {
		final VncVal fn = env.get(new VncSymbol("repl/term-rows"));
		if (Types.isVncFunction(fn)) {
			final VncVal cols = ((VncFunction)fn).applyOf();
			if (Types.isVncLong(cols)) {
				return ((VncLong)cols).getIntValue();
			}
		}
		
		return -1;
	}
	
	private static VncString getDoc(final VncVal val, final int width) {
		if (val != null && Types.isVncFunction(val)) {
			final VncFunction fn = (VncFunction)val;
			final VncList argsList = fn.getArgLists();
			final VncList examples = fn.getExamples();
			final VncList seeAlso = fn.getSeeAlso();
			
			final StringBuilder sb =  new StringBuilder();
						
			sb.append(argsList
						.stream()
						.map(s -> toString(s))
						.collect(Collectors.joining(", ")));
			
			sb.append("\n\n");
			sb.append(toString(fn.getDoc()));
			
			if (!examples.isEmpty()) {
				sb.append("\n\n");
				sb.append("EXAMPLES:\n");
				sb.append(examples
							.stream()
							.map(s -> toString(s))
							.map(e -> indent(e, "   "))
							.collect(Collectors.joining("\n\n")));
			}

			if (!seeAlso.isEmpty()) {
				sb.append("\n\n");
				sb.append("SEE ALSO:\n   ");
				sb.append(seeAlso
							.stream()
							.map(s -> toString(s))
							.collect(Collectors.joining(", ")));
			}

			sb.append("\n");

			return new VncString(sb.toString());			
		}
				
		return new VncString("<no documentation available>");			
	}
	
	private static String indent(final String text, final String indent) {
		if (StringUtil.isBlank(text)) {
			return text;
		}
		else {
			return StringUtil
						.splitIntoLines(text)
						.stream()
						.map(s -> indent + s)
						.collect(Collectors.joining("\n"));
		}
	}
	
	private static String toString(final VncVal val) {
		return val == Constants.Nil ? "" : ((VncString)val).getValue();
	}
}
