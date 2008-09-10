/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring;

import junit.framework.TestCase;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class LazyPortalApplicationContextTest extends TestCase {
    public void testLazyLoadingPortalApplicationContext() throws Exception {
        PortalApplicationContextLocator.getApplicationContext();
    }
}
