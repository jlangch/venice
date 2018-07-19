package com.github.jlangch.venice.impl;


public class Token {
	
	public Token(final String token, final String file, final int line, final int col) {
		this.token = token;
		this.file = file == null || file.isEmpty() ? "unknown" : file;
		this.line = line;
		this.col = col;
	}
	
	
	public String getToken() {
		return token;
	}
	
	public String getFile() {
		return file;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return col;
	}
	
	public char charAt(int index) {
		return token.charAt(index);
	}

	public boolean equals(String str) {
		return token.equals(str);
	}

	@Override
	public String toString() {
		return String.format("%s (file: %s, line %d, column %d)", token, file, line, col);
	}

	
	private final String token;
	private final String file; 
	private final int line; 
	private final int col;
}
