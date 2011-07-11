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
import java.io.Writer;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class TeeWriter extends Writer {

	private final Writer original;
	private final Writer branch;
	
	public TeeWriter(Writer original, Writer branch) {
		this.original = original;
		this.branch = branch;
	}
	/* (non-Javadoc)
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		this.original.close();
		this.branch.close();
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		this.original.flush();
		this.branch.flush();
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public synchronized void write(char[] cbuf, int off, int len) throws IOException {
		this.original.write(cbuf, off, len);
		this.branch.write(cbuf, off, len);
	}

}
