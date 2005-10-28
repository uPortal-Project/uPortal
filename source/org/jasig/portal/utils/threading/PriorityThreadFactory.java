/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.threading;

import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;

/**
 * Implementation of a ThreadFactory that allows you to set the thread
 * priority.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 *
 */
public class PriorityThreadFactory implements ThreadFactory {
    private final int threadPriority;
    
    public PriorityThreadFactory(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setPriority(this.threadPriority);
        return t;
    }
}
