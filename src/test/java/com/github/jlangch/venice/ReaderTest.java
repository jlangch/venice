package com.github.jlangch.venice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.jlangch.venice.impl.Reader;


public class ReaderTest {

	@Test
	public void testTokenize() {	
		final String s = 
				"(do               \n" +
				"   100            \n" +
				"   ;comment       \n" +
				"   \"abcdef\"     \n" +
				"   (+ 2 3)        \n" +
				")                   ";
		
		final Reader reader = new Reader(Reader.tokenize(s,"test"));
		//System.out.println(reader);
		assertNotNull(reader);
	}

}
