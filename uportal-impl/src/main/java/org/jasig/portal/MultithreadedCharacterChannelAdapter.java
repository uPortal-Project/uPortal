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
