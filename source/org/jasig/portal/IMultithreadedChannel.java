/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

import org.xml.sax.DocumentHandler;

/**
 * An interface for multithreaded channels.
 * Multithreaded channels are trusted to keep their own state/session/user 
 * separation (instead of relying on the servlet engine to do so).
 * The methods are exact analogs of those in {@link IChannel} interface, 
 * but means to identify the channel instance are passed along 
 * with each method.
 * Please refer to {@link IChannel} interface for method descriptions.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see IChannel
 * @see IMultithreadedCacheable
 */

public interface IMultithreadedChannel {

    /**
     * @param uid a string uniqly identifying a channel "instance" in the system.
     * For example, a combination of session id and channel instance id would fit the bill.
     */
    public void setStaticData (ChannelStaticData sd, String uid) throws PortalException;
    public void setRuntimeData (ChannelRuntimeData rd, String uid) throws PortalException;

    public void receiveEvent (PortalEvent ev,String uid);

    public ChannelRuntimeProperties getRuntimeProperties (String uid);
    public void renderXML (DocumentHandler out,String uid) throws PortalException;
}
