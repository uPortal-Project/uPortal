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

import java.io.PrintWriter;

import org.xml.sax.ContentHandler;

/**
 * Internal adaptor class that presents {@link IMultithreadedCharacterChannel} as a simple {@link IChannel}
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>, <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see IMultithreadedCharacterChannel
 */
public class MultithreadedCharacterChannelAdapter implements ICharacterChannel {
    final String uid;
    final IMultithreadedCharacterChannel channel;
    public MultithreadedCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid) {
        this.uid = uid;
        this.channel = channel;
    }
    public void setStaticData(ChannelStaticData sd) throws PortalException {
        channel.setStaticData(sd, this.uid);
    }
    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
        channel.setRuntimeData(rd, this.uid);
    }
    public void receiveEvent (PortalEvent ev) {
        channel.receiveEvent(ev, this.uid);
    }
    public ChannelRuntimeProperties getRuntimeProperties () {
        return channel.getRuntimeProperties(this.uid);
    }
    public void renderXML (ContentHandler out) throws PortalException {
        channel.renderXML(out, this.uid);
    }
    public void renderCharacters(PrintWriter pw) throws PortalException {
        channel.renderCharacters(pw, this.uid);
    }
}
