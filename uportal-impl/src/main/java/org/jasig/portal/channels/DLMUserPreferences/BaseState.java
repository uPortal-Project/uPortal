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

package org.jasig.portal.channels.DLMUserPreferences;

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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
