/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils.threading;

import junit.framework.TestCase;

/**
 * Testcase for BoundedThreadPool.
 * @version $Revision$ $Date$
 */
public class BoundedThreadPoolTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * This testcase exercises BoundedThreadPool to ensure that thread destroying
     * at least doesn't throw anything.
     * @throws Exception as a failure modality
     */
    public void testDestroyThread() throws Exception {
        BoundedThreadPool btp = new BoundedThreadPool(1, 1, 5);
        
        Thread thread = btp.createNewThread();
        
        // In BoundedThreadPool version 1.8, the following call resulted in
        // StackOverflowError
        
        btp.destroyThread(thread);

    }

}

