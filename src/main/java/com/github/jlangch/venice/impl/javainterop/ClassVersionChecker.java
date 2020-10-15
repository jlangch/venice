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
package com.github.jlangch.venice.impl.javainterop;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.jlangch.venice.impl.util.io.ClassPathResource;


/**
 * Reads the Java major version from a class file
 * 
 * <p>The definition of the class file format can be found here: 
 * <a href="https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html">Format</a>
 * 
 * <ul>
 * <li> Java 1.2 uses major version 46</li>
 * <li> Java 1.3 uses major version 47</li>
 * <li> Java 1.4 uses major version 48</li>
 * <li> Java 5 uses major version 49</li>
 * <li> Java 6 uses major version 50</li>
 * <li> Java 7 uses major version 51</li>
 * <li> Java 8 uses major version 52</li>
 * <li> Java 9 uses major version 53</li>
 * <li> Java 10 uses major version 54</li>
 * <li> Java 11 uses major version 55</li>
 * <li> Java 12 uses major version 56</li>
 * <li> Java 13 uses major version 57</li>
 * <li> Java 14 uses major version 58</li>
 * <li> Java 15 uses major version 59</li>
 * </ul>
 */
public class ClassVersionChecker {

	public static int getClassResourceMajorVersion(final String classResource) {
		try {		
			return readMajorVersion(
						new ClassPathResource(classResource).getInputStream(), 
						classResource);
		}
		catch(IOException ex) {
			throw new RuntimeException(
					"Failed to retrieve Java major version for class "
						+ "resource '" + classResource + "'!");
		}
	}

	public static int getClassFileMajorVersion(final String filename) {
		try {
			return readMajorVersion(
						new FileInputStream(filename), 
						filename);
		}
		catch(IOException ex) {
			throw new RuntimeException(
					"Failed to retrieve Java major version for file"
						+ " '" + filename + "'!");
		}
	}
	
	@SuppressWarnings("unused")
	private static int readMajorVersion(final InputStream in, final String source) throws IOException {
		try (DataInputStream din = new DataInputStream(in)) {
			
			final int magic = din.readInt();
			if (magic != MAGIC) {
				throw new RuntimeException(source + " is not a valid class!");
			}
			
			final int minor = din.readUnsignedShort();
			final int major = din.readUnsignedShort();
			
			return major;
		}
	}
	
	
	private static int MAGIC = 0xcafebabe; // 4 bytes
}
