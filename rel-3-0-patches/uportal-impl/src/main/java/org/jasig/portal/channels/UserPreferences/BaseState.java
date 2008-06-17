/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
