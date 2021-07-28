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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * UTF8FileReader
 *
 * I often want to read a file using UTF8 and it is annoying having
 * to create two objects every time.   This class just combines
 * the FileInputStream and the InputStreamReader into a single
 * class using the character encoding UTF8.   Very simple.
 *
 * Author: Keith Swenson Copyright: Keith Swenson, all rights reserved License:
 * This code is made available under the GNU Lesser GPL license.
 */
public class UTF8FileReader extends Reader {
	private InputStreamReader out;

	/**
	 * Creates the necessary objects to serve as a Writer
	 * to files.
	 */
	public UTF8FileReader(File file) throws Exception {
	    InputStream os = new FileInputStream(file);
		out = new InputStreamReader(os, "UTF-8");
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
    public int read(char[] arg0, int arg1, int arg2) throws IOException {
        return out.read(arg0, arg1, arg2);
    }
}
