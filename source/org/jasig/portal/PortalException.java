/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
 * An abstract base for the uPortal channel exceptions.
 * Information contained in this class allows ErrorChannel
 * to handle errors gracefully. 
 * @author Peter Kharchenko
 * @version $Revision$
 */

public abstract class PortalException extends Exception {
    // enumerating possible exceptions
    public static final int GENERAL_RENDERING_EXCEPTION=0;
    public static final int INTERNAL_TIMEOUT_EXCEPTION=1;
    public static final int AUTHORIZATION_EXCEPTION=2;
    public static final int RESOURCE_MISSING_EXCEPTION=3;

    // should the user be given an option to reinstantiate
    // the channel in a given session ?
    boolean b_reinst=true;
    // should the user be given an option to retry rendering
    // that same channel instance ?
    boolean b_refresh=true;

    public PortalException(boolean refresh, boolean reinstantiate) {
	b_reinst=reinstantiate;
    }

    public PortalException(String msg) {
	super(msg);
    }

    public PortalException(String msg, boolean refresh, boolean reinstantiate) {
	super(msg);
	b_reinst=reinstantiate;
    }

    public PortalException() {
    }

    abstract public int getExceptionCode();
    
    public boolean allowRefresh() {
	return b_refresh;
    }

    public boolean allowReinstantiation() {
	return b_reinst;
    }
}
