package com.github.jlangch.venice.impl.docgen;

public class CodeSnippet {

	public CodeSnippet(final String title, final String code) {
		this.title = title;
		this.code = code;
	}
	
	
	public String getTitle() {
		return title;
	}
	
	public String getCode() {
		return code;
	}


	private final String title;
	private final String code;
}
