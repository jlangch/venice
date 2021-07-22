package com.github.jlangch.venice.demo;

import org.jline.builtins.TTop;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;


public class Top {

	public static void main(final String[] args){
		try {
			final TerminalBuilder builder = TerminalBuilder
												.builder()
												.streams(System.in, System.out)
												.system(true)
												.jna(false);
			
			final Terminal terminal = builder.build();
			
			TTop.ttop(terminal, null, null, args);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
