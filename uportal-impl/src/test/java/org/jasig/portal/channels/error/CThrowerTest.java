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