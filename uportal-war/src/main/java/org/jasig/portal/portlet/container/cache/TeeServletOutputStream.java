/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlet.container.cache;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.apache.commons.io.output.TeeOutputStream;

/**
 * {@link ServletOutputStream} that delegates it's write methods to a {@link TeeOutputStream}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class TeeServletOutputStream extends ServletOutputStream {

	private final TeeOutputStream outputStream;
	
	/**
	 * @param outputStream
	 */
	public TeeServletOutputStream(TeeOutputStream outputStream) {
		this.outputStream = outputStream;
	}
	/**
	 * @see TeeOutputStream
	 * @param original the original stream to write
	 * @param branch the 2nd stream to write the same data
	 */
	public TeeServletOutputStream(OutputStream original, OutputStream branch) {
		this(new TeeOutputStream(original, branch));
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		this.outputStream.write(b);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.outputStream.write(b, off, len);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		this.outputStream.write(b);
	}

}
