package org.venice.impl;


public class TokenPos {
	
	public TokenPos(final int row, final int col) {
		this.row = row;
		this.col = col;
	}
	
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	@Override
	public String toString() {
		return String.format("(row %d, col %d)", row, col);
	}

	
	private final int row; 
	private final int col;
}
