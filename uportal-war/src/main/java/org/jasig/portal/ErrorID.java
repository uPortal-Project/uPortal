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

package org.jasig.portal;

/**
 * Represents a portal error or problem.
 * @author Howard Gilbert
 * @version $Revision$
 */
public class ErrorID {
	String category;
	String specific;
	String message;
	
	byte audience = 0;
	public final byte audienceAdmin = 0;
	public final byte audiencePgmr = 1;
	public final byte audienceUser = 2;
    
	byte duration = 0;
	public final byte durationRetry = 0;
	public final byte durationSession = 1;
	public final byte durationLater = 2;
	public final byte durationRestart = 3;
	public final byte durationMustfix = 4;
	
	
	/**
     * @param category
     * @param specific component/errorname as in "authenticate/badpassword"
     * @param msg default message text if not replaced from resources
     */
    public ErrorID(String category, String specific, String msg) {
		this.category=category;
		this.specific=specific;
		this.message=msg;
		ProblemsTable.register(this);
	}


    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return the specific
     */
    public String getSpecific() {
        return specific;
    }
	/**
	 * @return the audience
	 */
	public byte getAudience() {
		return audience;
	}

	/**
	 * @return the duration
	 */
	public byte getDuration() {
		return duration;
	}

	/**
	 * @param b
	 */
	public void setAudience(byte b) {
		audience = b;
	}

	/**
	 * @param b
	 */
	public void setDuration(byte b) {
		duration = b;
	}
	
	// initializer methods
	public ErrorID withAudience(byte b) {
		setAudience(b);
		return this;
	}
	
	public ErrorID withDuration(byte b) {
		setDuration(b);
		return this;
	}
	
}
