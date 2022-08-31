/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
package com.github.jlangch.venice.impl.util.loadpath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.github.jlangch.venice.impl.util.io.CharsetUtil;
import com.github.jlangch.venice.impl.util.io.zip.Zipper;
import com.github.jlangch.venice.javainterop.AcceptAllInterceptor;
import com.github.jlangch.venice.javainterop.LoadPathsFactory;

/**
 * Creates a set of files on the temp directory:
 *
 * <pre>
 *
 * root
 *  |
 *  +--- res1.txt
 *  |
 *  +--- dir1
 *  |     |
 *  |     +--- res2.txt
 *  |     +--- res3.txt
 *  |
 *  +--- dir2
 *
 * </pre>
 */
public class TempFS {

    private TempFS(final File root) {
        this.root = root;
    }

    public static TempFS create() {
        try {
            final File root = Files.createTempDirectory("loadpath_tempfs_").toFile();
            return new TempFS(root).init();
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to create TempFS", ex);
        }
    }

    public static void with(BiConsumer<TempFS,File> run) {
    	final TempFS tempFS = TempFS.create();
        try {
        	run.accept(tempFS, tempFS.root);
        }
        finally {
        	tempFS.remove();
        }
    }

    public void remove() {
        try {
            if (root.isDirectory()) {
                Files.walk(root.toPath())
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        }
        catch(IOException ex) {
            throw new RuntimeException("Failed to remove TempFS at " + root.getPath(), ex);
        }
    }

    public File getRoot() {
        return root;
    }

    public AcceptAllInterceptor createSandbox(boolean unlimited) {
    	return new AcceptAllInterceptor(
    					LoadPathsFactory.of(
					           Arrays.asList(
						           new File(root, "res1.txt"),
					               new File(root, "dir1"),
					               new File(root, "dir1/res2.txt"),
					               new File(root, "dir1/res3.txt"),
					               new File(root, "zip1.zip"),
					               new File(root, "dir1/zip2.zip")),
					           unlimited));
    }

    private TempFS init() {
        try {
            final File dir1  = new File(root, "dir1");
            final File dir11 = new File(root, "dir1/11");
            final File dir2  = new File(root, "dir2");
            dir1.mkdir();
            dir11.mkdir();
            dir2.mkdir();

            // venice files
            writeText("div.venice", "(defn func [] (/ 10 2))");

            writeText("dir1/sum.venice", "(defn func [] (+ 10 1))");

            writeText("dir1/sub.venice", "(defn func [] (- 10 1))");

            // resource files
            writeText("res1.txt", "res1");

            writeText("dir1/res2.txt", "res2");

            writeText("dir1/res3.txt", "res3");

            writeText("dir1/11/res4.txt", "res4");


            writeText("dir2/res5.txt", "res5");

            writeZip(
            	"zip1.zip",
            	"res11.txt",        "res11",
            	"dir-z1/res12.txt", "res12");

            writeZip(
            	"dir1/zip2.zip",
            	"res21.txt",        "res21",
            	"dir-z2/res22.txt", "res22");

            writeZip(
            	"dir2/zip3.zip",
            	"res31.txt",        "res31",
            	"dir-z3/res32.txt", "res32");
        }
        catch(IOException ex) {
            remove();
        }

        return this;
    }

    private void writeText(final String file, final String data) throws IOException {
        writeBinary(file, data.getBytes(CharsetUtil.DEFAULT_CHARSET));
    }

    private void writeBinary(final String file, final byte[] data) throws IOException {
        Files.write(new File(root, file).toPath(), data, StandardOpenOption.CREATE);
    }

    private void writeZip(
    		final String file,
    		final String entry1,
    		final String data1,
    		final String entry2,
    		final String data2
    ) throws IOException {
    	final Map<String, Object> entries = new HashMap<>();

    	entries.put(entry1, data1.getBytes(CharsetUtil.DEFAULT_CHARSET));
    	entries.put(entry2, data2.getBytes(CharsetUtil.DEFAULT_CHARSET));

    	writeBinary(file, Zipper.zip(entries));
    }

    private final File root;
}
