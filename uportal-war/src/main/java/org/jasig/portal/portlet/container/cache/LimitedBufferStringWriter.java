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

import java.io.StringWriter;

/**
 * Subclass of {@link StringWriter} that will only capture up to 
 * a specified threshold.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class LimitedBufferStringWriter extends StringWriter {

	private final long thresholdBytes;
	private boolean limitExceeded = false;
	/**
	 * 
	 * @param thresholdBytes
	 */
	public LimitedBufferStringWriter(long thresholdBytes) {
		super();
		this.thresholdBytes = thresholdBytes;
	}

	/**
	 * 
	 * @return
	 */
	public long getThresholdBytes() {
		return thresholdBytes;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isLimitExceeded() {
		return limitExceeded;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.StringWriter#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) {
		if(!passesThreshold(len)) {
			super.write(cbuf, off, len);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.StringWriter#write(int)
	 */
	@Override
	public void write(int c) {
		if(!passesThreshold(1)) {
			super.write(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.StringWriter#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) {
		if(!passesThreshold(len)) {
			super.write(str, off, len);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.StringWriter#write(java.lang.String)
	 */
	@Override
	public void write(String str) {
		if(!passesThreshold(str.length())) {
			super.write(str);
		}
	}
	
	/**
	 * Inspect the length of {@link #getBuffer()}.
	 * If the newBytes argument plus the buffer length exceeds the threshold, return true.
	 * 
	 * @param newBytes
	 * @return
	 */
	protected boolean passesThreshold(int newBytes) {
		if(limitExceeded) {
			//short circuit to skip buffer length check if we've already exceeded limit
			return true;
		}
		
		int currentLength = getBuffer().length();
		if(currentLength + newBytes > thresholdBytes) {
			this.limitExceeded = true;
			if(currentLength > 0) {
				getBuffer().delete(0, currentLength);
			}
			return true;
		} else {
			return false;
		}
	}
	
}
