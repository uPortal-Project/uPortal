/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import junit.framework.TestCase;

/**
 * Testcase for ChannelDefinition class.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ChannelDefinitionTest extends TestCase {

    public void testIsPortlet() {
        ChannelDefinition cd = new ChannelDefinition(72);
        
        cd.setJavaClass("org.jasig.portal.channels.CGenericXSLT");
        assertFalse(cd.isPortlet());
        
        cd.setJavaClass("org.jasig.portal.channels.portlet.CPortletAdapter");
        assertTrue(cd.isPortlet());
        
    }

}