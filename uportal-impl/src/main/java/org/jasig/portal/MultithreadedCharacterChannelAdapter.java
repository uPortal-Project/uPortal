/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import java.io.PrintWriter;

import org.xml.sax.ContentHandler;

/**
 * Internal adaptor class that presents {@link IMultithreadedCharacterChannel} as a simple {@link IChannel}
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}, <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see IMultithreadedCharacterChannel
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
