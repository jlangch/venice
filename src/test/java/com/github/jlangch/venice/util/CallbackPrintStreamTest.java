package com.github.jlangch.venice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


public class CallbackPrintStreamTest {

	@Test
	public void test() throws Exception {
		final PrintStream orgStdOut = System.out;
		final List<String> captured = new ArrayList<>();
		
		try {
	        System.setOut(new CallbackPrintStream(true, s -> captured.add(s)));
	        
	        System.out.println(100);
	        System.out.println("abc");
	        System.out.println("d\ne");
	        System.out.print(300);
	        System.out.print('-');
	        System.out.print(400);
	        System.out.flush();
	        System.out.println(500.0);
	        
			assertEquals(5, captured.size());
			assertEquals("100\n", captured.get(0));
			assertEquals("abc\n", captured.get(1));
			assertEquals("d\ne\n", captured.get(2));
			assertEquals("300-400", captured.get(3));
			assertEquals("500.0\n", captured.get(4));
		}
		finally {
			System.setOut(orgStdOut);
		}
	}

	
}
