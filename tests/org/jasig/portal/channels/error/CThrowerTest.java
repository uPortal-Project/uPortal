/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error;

import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;


import junit.framework.TestCase;

/**
 * Trivial testcase for CThrower, the exception-throwing test channel.
 * Tests that CThrower is well behaved for all methods except renderXML
 * and that it throws a Throwable when that method is invoked.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class CThrowerTest extends TestCase {

    private CThrower cThrower;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.cThrower = new CThrower();
    }

    public void testGetRuntimeProperties() {
        ChannelRuntimeProperties crp = this.cThrower.getRuntimeProperties();
        assertNotNull(crp);
    }

    public void testReceiveEvent() {
        this.cThrower.receiveEvent(PortalEvent.SESSION_DONE_EVENT);
    }

    public void testSetStaticData() {
        this.cThrower.setStaticData(new ChannelStaticData());
    }

    public void testSetRuntimeData() {
        this.cThrower.setRuntimeData(new ChannelRuntimeData());
    }

    public void testRenderXML() {
        try {
            ContentHandler handler = new DefaultHandler();
            this.cThrower.renderXML(handler);
        } catch (Throwable t) {
          // good
            return;
        }
        fail("CThrower should have thrown.");
    }

}