/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils.threading;

import java.util.concurrent.ThreadFactory;

/**
 * Implementation of a ThreadFactory that allows you to set the thread
 * priority.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @deprecated Use {@link org.springframework.scheduling.concurrent.CustomizableThreadFactory} instead
 */
@Deprecated
public class PriorityThreadFactory implements ThreadFactory {
    private final int threadPriority;
    private final ThreadGroup tg;
    private final String threadName;

    public PriorityThreadFactory(int threadPriority, final String threadPoolName, final ThreadGroup parentGroup) {
        this.threadPriority = threadPriority;
        threadName = threadPoolName;
        this.tg = new ThreadGroup(parentGroup, threadPoolName);
    }

    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(tg, runnable);
        t.setPriority(this.threadPriority);
        t.setName(threadName + "#" + t.getId());
        t.setDaemon(true);
        return t;
    }
}
