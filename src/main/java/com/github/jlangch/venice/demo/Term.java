package com.github.jlangch.venice.demo;

import java.util.Arrays;

import org.jline.builtins.Tmux;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;


public class Term {
	
	public static void main(final String[] args){
		try {
			final TerminalBuilder builder = TerminalBuilder
												.builder()
												.streams(System.in, System.out)
												.system(true)
												.jna(false);
			
			final Terminal terminal = builder.build();
			
			Tmux tm = new Tmux(terminal, System.err, Term::open);
			tm.run();
			tm.execute(System.out, System.err, Arrays.asList(args));
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static void open(Terminal terminal) {
		terminal.writer().println("Terminal opened");
	}
	
}
