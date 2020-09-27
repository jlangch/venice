/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2020 Venice
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.ParsedLine;

import com.github.jlangch.venice.impl.util.StringUtil;


public class FilePathCompleter {

	public FilePathCompleter(final List<File> loadPaths) {
		this.loadPaths = loadPaths == null ? new ArrayList<>() : new ArrayList<>(loadPaths);
		if (this.loadPaths.isEmpty()) {
			this.loadPaths.add(new File("."));
		}
	}

	public final List<Candidate> getCandidates(final ParsedLine cmdLine) {
		final List<Candidate> candidates = new ArrayList<>();

        final String line = cmdLine.line();

	   	if (p0.matcher(line).matches()) {
	   		// (load-file 
	   		candidates.add(candidate("\"", false));
    	}
	   	else if (p1.matcher(line).matches()) {
	   		// (load-file "               -> ""
	   		// (load-file "xxx/           -> "xxx/"
	   		// (load-file "xxx/y.venice   -> "xxx/y.venice"
    		final Matcher m = p1.matcher(line);
    		if (m.matches() && m.groupCount() > 0) {
				final String path = m.group(1);
				
				if (path.endsWith(".venice")) {
			   		candidates.add(candidate("\"", true));
				}
				else {
					loadPaths
						.stream()
						.map(dir -> listFiles(dir, path))
						.flatMap(List::stream)
        				.sorted()
        				.forEach(f -> candidates.add(candidate(f, f, true)));
				}
    		}
    	}
    	
    	return candidates;
	}
 
    private List<String> listFiles(final File root, final String dir) {
    	try {
    		final File root_ = root.getAbsoluteFile().getCanonicalFile();
    		final String sRoot_ = root_.getPath() + "/";
    		
    		final File start = new File(root_, dir);
    		
    		if (start.isFile()) {
    			return Arrays.asList(makeRelativeFile(sRoot_, start));
    		}
    		else if (start.isDirectory()) {
    		    return Files.walk(start.toPath())
			    			.map(Path::toFile)
				    		.filter(f -> f.getName().endsWith(".venice"))
			    			.map(f -> makeRelativeFile(sRoot_, f))
				    		.filter(f -> f != null)
				    		.sorted()
				    		.collect(Collectors.toList());
    		}
    		else {
        		return new ArrayList<>();
    		}
    	}
    	catch(Exception ex) {
    		return new ArrayList<>();
    	}
    }
    
    private String makeRelativeFile(final String root, final File file) {
		try {
			 return StringUtil.removeStart(file.getAbsoluteFile().getCanonicalPath(), root); 
		} 
		catch(Exception ex) {
			return null; 
		}	
    }
         
    private Candidate candidate(final String value, final boolean complete) {
    	return new Candidate(value, value, null, null, null, null, complete);
	}
    
    private Candidate candidate(final String value, final String display, final boolean complete) {
    	return new Candidate(value, display, null, null, null, null, complete);
	}

    
	private final Pattern p0 = Pattern.compile("^.*[(]load-file\\s*$");
	private final Pattern p1 = Pattern.compile("^.*[(]load-file\\s*[\"]([^\"]*)$");		

	private final List<File> loadPaths;
}
