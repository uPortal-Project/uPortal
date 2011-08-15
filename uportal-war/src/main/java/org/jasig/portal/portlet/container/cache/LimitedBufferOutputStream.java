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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ThresholdingOutputStream;

/**
 * Subclass of {@link ThresholdingOutputStream} that uses an internal {@link ByteArrayOutputStream} of the same
 * capacity for capturing content.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class LimitedBufferOutputStream extends ThresholdingOutputStream {

	private ByteArrayOutputStream stream;
	
	public LimitedBufferOutputStream(int threshold) {
		super(threshold);
		stream = new ByteArrayOutputStream(threshold);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.io.output.ThresholdingOutputStream#getStream()
	 */
	@Override
	protected OutputStream getStream() throws IOException {
		return stream;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.io.output.ThresholdingOutputStream#thresholdReached()
	 */
	@Override
	protected void thresholdReached() throws IOException {
		this.stream = null;
	}

	/**
	 * 
	 * @return the captured content, or null if the threshold was exceeded
	 */
	public byte[] getCapturedContent() {
		if(stream != null) {
			return this.stream.toByteArray();
		}
		
		return null; 
	}
}
