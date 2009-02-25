/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
