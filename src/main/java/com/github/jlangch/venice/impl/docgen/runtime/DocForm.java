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
package com.github.jlangch.venice.impl.docgen.runtime;

import java.util.List;
import java.util.stream.Collectors;

import org.jline.utils.Levenshtein;

import com.github.jlangch.venice.VncException;
import com.github.jlangch.venice.impl.ModuleLoader;
import com.github.jlangch.venice.impl.Modules;
import com.github.jlangch.venice.impl.ansi.AnsiColorTheme;
import com.github.jlangch.venice.impl.ansi.AnsiColorThemes;
import com.github.jlangch.venice.impl.env.Env;
import com.github.jlangch.venice.impl.functions.CoreFunctions;
import com.github.jlangch.venice.impl.reader.HighlightItem;
import com.github.jlangch.venice.impl.reader.HighlightParser;
import com.github.jlangch.venice.impl.specialforms.SpecialFormsDoc;
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
import com.github.jlangch.venice.impl.types.custom.VncProtocol;
import com.github.jlangch.venice.impl.types.custom.VncWrappingTypeDef;
import com.github.jlangch.venice.impl.types.util.Types;
import com.github.jlangch.venice.impl.util.MetaUtil;
import com.github.jlangch.venice.impl.util.StringUtil;
import com.github.jlangch.venice.impl.util.Tuple2;
import com.github.jlangch.venice.impl.util.markdown.Markdown;


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

	public static VncString highlight(final VncString form, final Env env) {
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
				if (sym.hasNamespace()) {
					throw ex;
				}
				
				final List<VncSymbol> globalSymbols = env.getAllGlobalFunctionSymbols();
				final String simpleName = sym.getSimpleName();
				
				// exact match on simple name
				List<VncSymbol> candidates = getGlobalSymbolCandidates(
													simpleName, 
													globalSymbols,
													5,
													0);
				
				if (candidates.isEmpty()) {
					// levenshtein match on simple name with distance 1
					candidates = getGlobalSymbolCandidates(
									simpleName, 
									globalSymbols,
									5,
									1);
				}
				
				if (candidates.isEmpty()) {
					throw ex;
				}

				return new VncString(getSymbolNotFoundMsg(sym, candidates));
			}
		}
	}

	private static VncString docForSymbolVal(final VncVal symVal, final Env env) {
		final boolean repl = isREPL(env);

		// if we run in a REPL use the effective terminal width for rendering
		final int width = repl ? replTerminalWidth(env) : 80;
		
		return formatDoc(symVal, width);
	}
	
	private static VncString docForKeyword(final VncKeyword keyword, final Env env) {
		if (keyword.getValue().endsWith(".venice")) {
			if (ModuleLoader.isLoadedClasspathFile(keyword.getValue())) {
				return docForLoadedClasspathFile(keyword, env);
			}
			else if (ModuleLoader.isLoadedExternalFile(keyword.getValue())) {
				return docForLoadedExternalFile(keyword, env);
			}
			else {
				throw new VncException(String.format(
						"'%s' is not a loaded classpath file or an external file. "
							+ "No documentation available!",
						keyword.getValue()));
			}
		}
		else if (Modules.isValidModule(keyword)) {
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
		
		return highlightVeniceSource(form, env);
	}

	private static VncString docForLoadedClasspathFile(
			final VncString file, 
			final Env env
	) {
		final String form = ModuleLoader.getCachedClasspathFile(file.getValue());
		if (form == null) {
			throw new VncException(String.format(
					"The Venice source file '%s' has not been loaded yet from the classpath!", 
					file.getValue()));
		}
		
		return highlightVeniceSource(form, env);
	}

	private static VncString docForLoadedExternalFile(
			final VncString file, 
			final Env env
	) {
		final String form = ModuleLoader.getCachedExternalFile(file.getValue());
		if (form == null) {
			throw new VncException(String.format(
					"The Venice source file '%s' has not been loaded from the filesystem yet!", 
					file.getValue()));
		}
		
		return highlightVeniceSource(form, env);
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
			return new VncString(getDoc(type, (VncCustomTypeDef)tdef));
		}
		else if (tdef instanceof VncWrappingTypeDef) {
			return new VncString(getDoc(type, (VncWrappingTypeDef)tdef));
		}
		else if (tdef instanceof VncChoiceTypeDef) {
			return new VncString(getDoc(type, (VncChoiceTypeDef)tdef));
		}
		else {
			throw new VncException(String.format(
					":%s is not a custom type. Please qualify the type with its namespace!",
					type.getValue()));
		}
	}

	private static String getDoc(
			final VncKeyword type,
			final VncCustomTypeDef typeDef
	) {
		final StringBuilder sb = new StringBuilder();
		
		final int maxFieldLen = typeDef
									.getFieldDefs()
									.stream()
									.mapToInt(f -> f.getName().getValue().length())
									.max()
									.orElse(0);
		
		sb.append(String.format("Custom type: %s\n\n", typeDef.getType()));
		
		sb.append("Fields: \n");
		typeDef.getFieldDefs().forEach(f -> sb.append(String.format(
														"   %s: %s\n", 
														StringUtil.padRight(
															f.getName().getValue(),
															maxFieldLen),
														f.isNillable()
															? f.getType().getValue() + "?"
															: f.getType().getValue())));
		if (typeDef.getValidationFn() != null) {
			sb.append(String.format(
						"\nValidation function: %s\n", 
						typeDef.getValidationFn().getQualifiedName()));
		}
		
		return sb.toString();
	}

	private static String getDoc(
			final VncKeyword type,
			final VncWrappingTypeDef typeDef
	) {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Custom wrapped type: %s\n", type.getValue()));
		sb.append(String.format("Base type: %s\n", typeDef.getBaseType().getValue()));
		if (typeDef.getValidationFn() != null) {
			sb.append(String.format(
					"Validation function: %s\n", 
					typeDef.getValidationFn().getQualifiedName()));
		}
		
		return sb.toString();
	}

	private static String getDoc(
			final VncKeyword type,
			final VncChoiceTypeDef typeDef
	) {
		final VncSet types = typeDef.typesOnly();
		final VncSet values = typeDef.valuesOnly();
		
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("Custom choice type: %s\n", type.getValue()));
		if (!types.isEmpty()) {
			sb.append("Types: \n");
			typeDef.typesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
		}
		if (!values.isEmpty()) {
			sb.append("Values: \n");
			typeDef.valuesOnly().forEach(v -> sb.append(String.format("   %s\n", v.toString())));
		}
		
		return sb.toString();
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
		final VncVal fn = env.get(new VncSymbol("repl/term-cols"));
		if (Types.isVncFunction(fn)) {
			final VncVal cols = ((VncFunction)fn).applyOf();
			if (Types.isVncLong(cols)) {
				final int termWidth = ((VncLong)cols).getIntValue();
				if (termWidth <= 100) {
					return termWidth;
				}
				else {
					// shrink the terminal width used by the doc a bit
					// for wide terminals as the UNIX man tool is doing
					return 100 + (termWidth - 100) / 2;
				}
			}
		}
		
		return -1;
	}
	
	private static VncString formatDoc(final VncVal val, final int width) {
		if (val != null) {
			if (Types.isVncFunction(val)) {
				return formatDoc((VncFunction)val, width);
			}
			else if (Types.isVncProtocol(val)) {
				return formatDoc((VncProtocol)val, width);
			}
		}
				
		return new VncString(NO_DOC);			
	}
	
	private static VncString formatDoc(final VncFunction fn, final int width) {
		final VncList argsList = fn.getArgLists();
		final VncList examples = fn.getExamples();
		final VncList seeAlso = fn.getSeeAlso();
		
		final StringBuilder sb =  new StringBuilder();
					
		sb.append(argsList
					.stream()
					.map(s -> toString(s))
					.collect(Collectors.joining(", ")));
		
		sb.append("\n\n");
		
		final String fnDescr = toString(fn.getDoc());
		sb.append(MARKDOWN_FN_DESCR
					? Markdown.parse(fnDescr).renderToText(width)
					: fnDescr);
					
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
	
	private static VncString formatDoc(final VncProtocol protocol, final int width) {
		final VncVal doc = MetaUtil.getMetaVal(protocol.getMeta(), MetaUtil.DOC);
		final VncVal examples = MetaUtil.getMetaVal(protocol.getMeta(), MetaUtil.EXAMPLES);
		
		boolean empty = true;
		
		final StringBuilder sb =  new StringBuilder();

		if (Types.isVncString(doc)) {
			empty = false;
			final String descr = ((VncString)doc).getValue();
			sb.append(MARKDOWN_FN_DESCR
						? Markdown.parse(descr).renderToText(width)
						: descr);
		}

		if (Types.isVncList(examples)) {
			final VncList examples_ = (VncList)examples;
			if (!examples_.isEmpty()) {
				empty = false;
				sb.append("\n\n");
				sb.append("EXAMPLES:\n");
				sb.append(examples_
							.stream()
							.map(s -> toString(s))
							.map(e -> indent(e, "   "))
							.collect(Collectors.joining("\n\n")));
				sb.append("\n");
			}
		}

		return empty ? new VncString(NO_DOC)
					 : new VncString(sb.toString());
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
	
	private static VncString highlightVeniceSource(final String form, final Env env) {
		final AnsiColorTheme theme = AnsiColorThemes.getTheme(getColorTheme(env));

		if (theme == null) {
			return new VncString(form);
		}
		else {
			// frame the form with "(do  ... )" before parsing
			List<HighlightItem> items = HighlightParser.parse("(do " + form + ")");
			
			// remove the framing "(do  ... )" after parsing
			items = items.subList(3, items.size()-1);
			
			// style the items 
			return new VncString(
					AnsiColorTheme.ANSI_RESET +
					items.stream()
						 .map(it -> theme.style(it.getForm(), it.getClazz()))
						 .collect(Collectors.joining()));
		}
	}
	
	private static List<VncSymbol> getGlobalSymbolCandidates(
			final String name, 
			final List<VncSymbol> globalFunctionSymbols,
			final int limit,
			final int levenshteinDistance
	) {
		if (levenshteinDistance == 0) {
			return globalFunctionSymbols
					   .stream()
					   .filter(s -> s.getSimpleName().equals(name))
					   .sorted()
					   .limit(limit)
					   .collect(Collectors.toList());
		}
		else {
			return globalFunctionSymbols
					   .stream()
					   .map(s -> new Tuple2<Integer,VncSymbol>(
							   		Levenshtein.distance(name, s.getSimpleName()),
							   		s))
					   .filter(t -> t.getFirst() <= levenshteinDistance)
					   .sorted()
					   .map(t -> t.getSecond())
					   .limit(limit)
					   .collect(Collectors.toList());
		}
	}
	
	private static String getSymbolNotFoundMsg(
			final VncSymbol sym,
			final List<VncSymbol> candidates
	) {
		final List<String> indented = candidates
									   .stream()
									   .map(s -> "   " + s.getQualifiedName())
									   .collect(Collectors.toList());
		
		return String.format(
				"Symbol '%s' not found!\n\nDid you mean?\n%s\n", 
				sym.getQualifiedName(),
				String.join("\n", indented));
	}
	
	
	private static final boolean MARKDOWN_FN_DESCR = true;
	
	private static final String NO_DOC = "<no documentation available>";
}
