/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.support;

import junit.framework.TestCase;

/**
 * JUnit testcase for TitledChannelRuntimeProperties.
 */
public class TitledChannelRuntimePropertiesTest extends TestCase {

    /**
     * Test storing and retrieving a title.
     */
    public void testGetChannelTitle() {
        
        TitledChannelRuntimeProperties titledProperties = new TitledChannelRuntimeProperties("dynamic_title");
        
        assertEquals("dynamic_title", titledProperties.getChannelTitle());

    }

}
