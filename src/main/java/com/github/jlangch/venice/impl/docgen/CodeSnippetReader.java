package com.github.jlangch.venice.impl.docgen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jlangch.venice.impl.util.ClassPathResource;
import com.github.jlangch.venice.impl.util.StringUtil;


public class CodeSnippetReader {

	public List<CodeSnippet> readSnippets() {
		try {
			final List<String> lines = load();
			
			final List<Integer> snippetStartLines = new ArrayList<>();			
			for(int ii=0; ii<lines.size(); ii++) {
				if (lines.get(ii).startsWith("**")) {
					snippetStartLines.add(ii);
				}
			}
			snippetStartLines.add(lines.size());
			
			final List<CodeSnippet> snippets = new ArrayList<>(); 
			for(int ii=0; ii<snippetStartLines.size()-1; ii++) {
				snippets.add(
						new CodeSnippet(
								lines.get(snippetStartLines.get(ii)).substring(2),
								lines
									.subList(snippetStartLines.get(ii)+1, snippetStartLines.get(ii+1))
									.stream()
									.collect(Collectors.joining("\n"))
									.trim()));
			}

			return snippets;
		}
		catch(Exception ex) {
			throw new RuntimeException("Failed to load code snippets", ex);
		}
	}
	
	private List<String> load() throws Exception {
		return StringUtil.splitIntoLines(
				new ClassPathResource(
							"com/github/jlangch/venice/impl/docgen/cheatsheet.snippets")
						.getResourceAsString("UTF-8"));
	}

}
