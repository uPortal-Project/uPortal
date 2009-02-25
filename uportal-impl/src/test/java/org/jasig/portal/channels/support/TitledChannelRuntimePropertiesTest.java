/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
