/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
