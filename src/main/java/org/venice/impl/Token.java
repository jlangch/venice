package org.venice.impl;


public class Token {
	
	public Token(final String token, final int row, final int col) {
		this.token = token;
		this.row = row;
		this.col = col;
	}
	
	
	public String getToken() {
		return token;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}

	public TokenPos getPos() {
		return new TokenPos(row, col);
	}
	
	public char charAt(int index) {
		return token.charAt(index);
	}

	public boolean equals(String str) {
		return token.equals(str);
	}

	@Override
	public String toString() {
		return String.format("%s (row %d, col %d)", token, row, col);
	}

	
	private final String token;
	private final int row; 
	private final int col;
}
