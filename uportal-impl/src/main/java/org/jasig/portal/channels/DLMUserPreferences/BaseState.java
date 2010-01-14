/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
