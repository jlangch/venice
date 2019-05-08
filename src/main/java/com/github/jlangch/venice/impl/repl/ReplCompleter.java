/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2019 Venice
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
package com.github.jlangch.venice.impl.repl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import com.github.jlangch.venice.impl.VeniceInterpreter;

public class ReplCompleter implements Completer {
	
	public ReplCompleter(final VeniceInterpreter venice) {
		this.venice = venice;
	}

    public void complete(
    		final LineReader reader, 
    		final ParsedLine line, 
    		final List<Candidate> candidates
    ) {
    	if (line.line().endsWith("(load-file ")) {
    		for (String f : listFileNames()) {
   		    	candidates.add(new Candidate("\"" + f + "\""));
    		}
    	}
    	else if (line.line().endsWith("(load-file \"")) {
    		for (String f : listFileNames()) {
   		    	candidates.add(new Candidate(f));
    		}
    	}
    	else if (line.line().endsWith("(load-module ")) {
    		for (String m : venice.getAvailableModules()) {
   		    	candidates.add(new Candidate(":" + m));
    		}
    	}

    	
//    	System.err.println("word(): " + line.word());
//    	System.err.println("wordCursor(): " + line.wordCursor());
//    	System.err.println("wordIndex(): " + line.wordIndex());
//    	System.err.println("words(): " + line.words());
//    	System.err.println("line(): " + line.line());
//    	System.err.println("cursor(): " + line.cursor());
    }
    
    private List<String> listFileNames() {
    	return Arrays
	    		.stream(new File(".").listFiles())
	    		.filter(f -> f.isFile())
	    		.filter(f -> f.getName().endsWith("venice"))
	    		.map(f -> f.getName())
	    		.sorted()
	    		.collect(Collectors.toList());
     }
    
    
    private final VeniceInterpreter venice;
}
