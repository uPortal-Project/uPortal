/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
