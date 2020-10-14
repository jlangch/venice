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

import com.github.jlangch.venice.impl.util.io.ClassPathResource;


/**
 * Reads the Java major version from a class file
 * 
 * <ul>
 * <li> Java 1.2 uses major version 46
 * <li> Java 1.3 uses major version 47
 * <li> Java 1.4 uses major version 48
 * <li> Java 5 uses major version 49
 * <li> Java 6 uses major version 50
 * <li> Java 7 uses major version 51
 * <li> Java 8 uses major version 52
 * <li> Java 9 uses major version 53
 * <li> Java 10 uses major version 54
 * <li> Java 11 uses major version 55
 * <li> Java 12 uses major version 56
 * <li> Java 13 uses major version 57
 * <li> Java 14 uses major version 58
 * <li> Java 15 uses major version 59
 * </ul>
 */
public class ClassVersionChecker {

	@SuppressWarnings("unused")
	public static int getClassResourceMajorVersion(final String classResource) {
		try (DataInputStream in = new DataInputStream(
									new ClassPathResource(classResource)
											.getInputStream())) {
			
			final int magic = in.readInt();
			if (magic != MAGIC) {
				throw new RuntimeException(
						classResource + " is not a valid class!");
			}
			
			final int minor = in.readUnsignedShort();
			final int major = in.readUnsignedShort();
			
			return major;
		}
		catch(IOException ex) {
			throw new RuntimeException(
					"Failed to retrieve Java major version for class "
						+ "resource '" + classResource + "'!");
		}
	}

	@SuppressWarnings("unused")
	public static int getClassFileMajorVersion(final String filename) {
		try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
			
			final int magic = in.readInt();
			if (magic != MAGIC) {
				throw new RuntimeException(filename + " is not a valid class!");
			}
			
			final int minor = in.readUnsignedShort();
			final int major = in.readUnsignedShort();
			
			return major;
		}
		catch(IOException ex) {
			throw new RuntimeException(
					"Failed to retrieve Java major version for file"
						+ " '" + filename + "'!");
		}
	}
	
	
	private static int MAGIC = 0xcafebabe;
}
