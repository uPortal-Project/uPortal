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

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/** <p>A base class for a CUserPreferences state.</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */


class BaseState implements IPrivilegedChannel {
    protected CUserPreferences context;
    protected IPrivilegedChannel internalState;

    public BaseState() {}

    public BaseState(CUserPreferences context) {
        this.context=context;
    }

    public BaseState(IPrivilegedChannel state) {
        internalState=state;
    }

    public BaseState(CUserPreferences context,IPrivilegedChannel state) {
        internalState=state;
    }

    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException  {
    }

    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        // analyze header parameters, reset states, etc.
    }

    public void setStaticData(ChannelStaticData sd) throws PortalException  {
    };

    public void renderXML (ContentHandler out) throws PortalException {
        // render header controls
    }

    public void receiveEvent (PortalEvent ev){}

    // these two functions are never really called
    public ChannelRuntimeProperties getRuntimeProperties () { return new ChannelRuntimeProperties(); }

    public void setState(IPrivilegedChannel state) {
        this.internalState=state;
    }

    public void setContext(CUserPreferences context) {
        this.context=context;
    }
}
