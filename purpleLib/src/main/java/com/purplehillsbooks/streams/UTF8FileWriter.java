/*
 * Copyright 2013 Keith D Swenson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purplehillsbooks.streams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * UTF8FileWriter
 *
 * I often want to write to a file using UTF8 and it is annoying having
 * to create two objects every time.   This class just combines
 * the FileOutputStream and the OutputStreamWriter into a single
 * class using the character encoding UTF8.   Very simple.
 *
 * Author: Keith Swenson Copyright: Keith Swenson, all rights reserved License:
 * This code is made available under the GNU Lesser GPL license.
 */
public class UTF8FileWriter extends Writer {
	private OutputStreamWriter out;

	/**
	 * Creates the necessary objects to serve as a Writer
	 * to files.
	 */
	public UTF8FileWriter(File file) throws Exception {
	    OutputStream os = new FileOutputStream(file);
		out = new OutputStreamWriter(os, "UTF-8");
	}


	/**
	 * Flush.
	 *
	 * @exception IOException
	 *                if the original Writer fails to be flushed
	 */
    @Override
	public void flush() throws IOException {
		out.flush();
	}

	/**
	 * Closes the wrapped OutputStream and terminates output.
	 *
	 * @exception IOException
	 *                if the original Writer fails to be closed
	 */
    @Override
	public void close() throws IOException {
		out.close();
	}

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }
}
